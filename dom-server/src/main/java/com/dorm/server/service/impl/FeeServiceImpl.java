package com.dorm.server.service.impl;

import com.dorm.server.constant.MessageConstants;
import com.dorm.server.constant.SystemConstants;
import com.dorm.server.entity.Fee;
import com.dorm.server.entity.common.PageVO;
import com.dorm.server.entity.dto.CalculateFeeDTO;
import com.dorm.server.entity.dto.ConfirmFeeDTO;
import com.dorm.server.entity.dto.GenerateFeeDTO;
import com.dorm.server.entity.dto.UpdateFeeDTO;
import com.dorm.server.entity.vo.CheckinVO;
import com.dorm.server.entity.vo.FeeVO;
import com.dorm.server.entity.vo.GenerateFeeResultVO;
import com.dorm.server.exception.BusinessException;
import com.dorm.server.mapper.FeeMapper;
import com.dorm.server.mapper.ResidenceHistoryMapper;
import com.dorm.server.service.FeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * 寮費业务实现类
 *
 * @author dorm-server
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeeServiceImpl implements FeeService {

    private final FeeMapper feeMapper;
    private final ResidenceHistoryMapper residenceHistoryMapper;

    @Override
    public PageVO<FeeVO> listFees(Integer page, Integer pageSize, String status,
                                  String employeeId, Integer dormitoryId,
                                  String periodStart, String periodEnd) {
        int offset = (page - 1) * pageSize;
        List<FeeVO> items = feeMapper.selectPageList(status, employeeId, dormitoryId,
                periodStart, periodEnd, offset, pageSize);
        Long total = feeMapper.selectPageCount(status, employeeId, dormitoryId, periodStart, periodEnd);

        log.info("[费用列表] page={}, status={}, total={}", page, status, total);
        return PageVO.of(items, total, page, pageSize);
    }

    @Override
    public FeeVO getFeeById(Long id) {
        FeeVO vo = feeMapper.selectVoById(id);
        if (vo == null) {
            throw new BusinessException(MessageConstants.FEE_NOT_FOUND);
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FeeVO calculateFee(CalculateFeeDTO dto) {
        log.info("[计算费用] residenceId={}, period={} ~ {}", dto.getResidenceId(),
                dto.getPeriodStart(), dto.getPeriodEnd());

        // 查询入居记录（含宿舍关联信息）
        CheckinVO checkin = residenceHistoryMapper.selectCheckinVoById(dto.getResidenceId());
        if (checkin == null) {
            throw new BusinessException(MessageConstants.CHECKIN_NOT_FOUND);
        }

        // 计算在住天数
        LocalDate start = dto.getPeriodStart();
        LocalDate end = dto.getPeriodEnd();
        long stayDays = ChronoUnit.DAYS.between(start, end) + 1;

        // 构建费用实体并保存
        Fee fee = new Fee();
        fee.setResidenceId(dto.getResidenceId());
        fee.setEmployeeId(checkin.getEmployeeId());
        fee.setEmployeeName(checkin.getEmployeeName());
        fee.setDormitoryId(checkin.getDormitoryId());
        fee.setDormitoryName(checkin.getDormitoryName());
        fee.setRoomId(checkin.getRoomId());
        fee.setRoomName(checkin.getRoomNumber());
        fee.setPeriodStart(start);
        fee.setPeriodEnd(end);
        fee.setStayDays((int) stayDays);

        // 日額：直接使用 DTO 传入的 dailyRate（前端或调用方负责传入正确金额）
        BigDecimal dailyRate = dto.getDailyRate();
        fee.setDailyRate(dailyRate);
        fee.setBaseAmount(dailyRate.multiply(BigDecimal.valueOf(stayDays)));
        fee.setDailySuppliesCost(BigDecimal.ZERO);
        fee.setTotalAmount(fee.getBaseAmount().add(fee.getDailySuppliesCost()));
        fee.setStatus(SystemConstants.FEE_STATUS_PENDING);

        feeMapper.insert(fee);

        return feeMapper.selectVoById(fee.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmFees(ConfirmFeeDTO dto) {
        if (CollectionUtils.isEmpty(dto.getFeeIds())) {
            throw new BusinessException(MessageConstants.FEE_ID_LIST_EMPTY);
        }
        log.info("[批量确认费用] count={}", dto.getFeeIds().size());

        int affected = feeMapper.batchConfirm(dto.getFeeIds());
        log.info("[批量确认费用] 实际确认={}条", affected);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GenerateFeeResultVO generateMonthlyFees(GenerateFeeDTO dto) {
        // 対象月の月初・月末を算出
        LocalDate monthStart = LocalDate.of(dto.getYear(), dto.getMonth(), 1);
        LocalDate monthEnd   = YearMonth.of(dto.getYear(), dto.getMonth()).atEndOfMonth();

        log.info("[月次一括生成] 対象月={}/{}, period={} ~ {}",
                dto.getYear(), dto.getMonth(), monthStart, monthEnd);

        // 対象月に在寮していた全入居記録を取得（日額も JOIN して取得済み）
        List<Map<String, Object>> activeList = feeMapper.selectActiveInMonth(monthStart, monthEnd);
        if (CollectionUtils.isEmpty(activeList)) {
            log.info("[月次一括生成] 対象入居者なし、処理をスキップ");
            GenerateFeeResultVO emptyResult = new GenerateFeeResultVO();
            emptyResult.setGenerated(0);
            emptyResult.setSkipped(0);
            emptyResult.setTotal(0);
            return emptyResult;
        }

        int generated = 0;
        int skipped   = 0;

        for (Map<String, Object> row : activeList) {
            // 入居記録 ID
            Integer residenceId = toInteger(row.get("id"));

            // 実際の在寮期間 ∩ 指定月の重複区間を算出
            LocalDate checkInDate  = toLocalDate(row.get("check_in_date"));
            LocalDate checkOutDate = row.get("check_out_date") != null
                    ? toLocalDate(row.get("check_out_date")) : null;

            // 費用期間 = max(入寮日, 月初) ～ min(退寮日 or 月末, 月末)
            LocalDate periodStart = checkInDate.isAfter(monthStart) ? checkInDate : monthStart;
            LocalDate periodEnd   = (checkOutDate != null && checkOutDate.isBefore(monthEnd))
                    ? checkOutDate : monthEnd;

            // 重複チェック：同一入居 ID・同一期間の費用が既存であればスキップ
            int dupCount = feeMapper.countByResidenceAndPeriod(residenceId, periodStart, periodEnd);
            if (dupCount > 0) {
                log.debug("[月次一括生成] スキップ: residenceId={}, period={} ~ {}",
                        residenceId, periodStart, periodEnd);
                skipped++;
                continue;
            }

            // 在住日数を算出（periodStart〜periodEnd の両端含む）
            long stayDays = ChronoUnit.DAYS.between(periodStart, periodEnd) + 1;

            // 日額：SQL 側で COALESCE(rooms.daily_rate, dormitories.daily_rate) 済み
            BigDecimal dailyRate = toBigDecimal(row.get("daily_rate"));
            if (dailyRate == null || dailyRate.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("[月次一括生成] 日額が未設定または0: residenceId={}, スキップ", residenceId);
                skipped++;
                continue;
            }

            // 費用エンティティを組み立てて保存
            Fee fee = new Fee();
            fee.setResidenceId(residenceId);
            fee.setEmployeeId(toString(row.get("employee_id")));
            fee.setEmployeeName(toString(row.get("employee_name")));
            fee.setDormitoryId(toInteger(row.get("dormitory_id")));
            fee.setDormitoryName(toString(row.get("dormitory_name")));
            fee.setRoomId(toInteger(row.get("room_id")));
            fee.setRoomName(toString(row.get("room_name")));
            fee.setPeriodStart(periodStart);
            fee.setPeriodEnd(periodEnd);
            fee.setStayDays((int) stayDays);
            fee.setDailyRate(dailyRate);
            fee.setBaseAmount(dailyRate.multiply(BigDecimal.valueOf(stayDays)));
            fee.setDailySuppliesCost(BigDecimal.ZERO);
            fee.setTotalAmount(fee.getBaseAmount().add(fee.getDailySuppliesCost()));
            fee.setStatus(SystemConstants.FEE_STATUS_PENDING);

            feeMapper.insert(fee);
            generated++;

            log.debug("[月次一括生成] 生成: residenceId={}, days={}, amount={}",
                    residenceId, stayDays, fee.getTotalAmount());
        }

        log.info("[月次一括生成] 完了: total={}, generated={}, skipped={}",
                activeList.size(), generated, skipped);

        // 結果 VO を組み立てて返却
        GenerateFeeResultVO result = new GenerateFeeResultVO();
        result.setGenerated(generated);
        result.setSkipped(skipped);
        result.setTotal(activeList.size());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FeeVO updateFee(Long id, UpdateFeeDTO dto) {
        log.info("[費用編集] id={}, period={} ~ {}", id, dto.getPeriodStart(), dto.getPeriodEnd());

        // 対象レコードを取得（存在チェック）
        Fee fee = feeMapper.selectById(id);
        if (fee == null) {
            throw new BusinessException(MessageConstants.FEE_NOT_FOUND);
        }

        // pending ステータスのみ編集可能
        if (!SystemConstants.FEE_STATUS_PENDING.equals(fee.getStatus())) {
            throw new BusinessException("確定済みまたは支払済みの費用は編集できません");
        }

        // 日付バリデーション：終了日は開始日より後であること
        if (!dto.getPeriodEnd().isAfter(dto.getPeriodStart())) {
            throw new BusinessException("終了日は開始日より後の日付を指定してください");
        }

        // 在住日数を再計算（両端含む）
        long stayDays = ChronoUnit.DAYS.between(dto.getPeriodStart(), dto.getPeriodEnd()) + 1;

        // 金額を再計算（日用品費は常に0、totalAmount = baseAmount）
        BigDecimal baseAmount  = dto.getDailyRate().multiply(BigDecimal.valueOf(stayDays));
        BigDecimal totalAmount = baseAmount;

        // エンティティに反映
        fee.setPeriodStart(dto.getPeriodStart());
        fee.setPeriodEnd(dto.getPeriodEnd());
        fee.setStayDays((int) stayDays);
        fee.setDailyRate(dto.getDailyRate());
        fee.setBaseAmount(baseAmount);
        fee.setDailySuppliesCost(BigDecimal.ZERO);
        fee.setTotalAmount(totalAmount);

        // DB 更新（pending かつ未削除が条件）
        int affected = feeMapper.updateById(fee);
        if (affected == 0) {
            throw new BusinessException("更新に失敗しました。対象レコードが存在しないか編集不可の状態です");
        }

        log.info("[費用編集] 完了: id={}, stayDays={}, totalAmount={}", id, stayDays, totalAmount);
        return feeMapper.selectVoById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFee(Long id) {
        log.info("[费用删除] id={}", id);

        // 查询费用记录（含软删除检查）
        Fee fee = feeMapper.selectById(id);
        if (fee == null) {
            throw new BusinessException(MessageConstants.FEE_NOT_FOUND);
        }

        // 仅允许删除 pending 状态的费用
        if (!SystemConstants.FEE_STATUS_PENDING.equals(fee.getStatus())) {
            throw new BusinessException(MessageConstants.FEE_NOT_PENDING);
        }

        int affected = feeMapper.softDeleteById(id);
        if (affected == 0) {
            throw new BusinessException(MessageConstants.FEE_NOT_FOUND);
        }

        log.info("[费用删除] 完了: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteFees(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BusinessException(MessageConstants.FEE_DELETE_IDS_EMPTY);
        }
        log.info("[批量费用删除] count={}", ids.size());

        int affected = feeMapper.batchDelete(ids);
        log.info("[批量费用删除] 实际删除={}条", affected);
    }

    // ── 内部ユーティリティメソッド ──────────────────────────────────────────

    /**
     * Map から取得した値を Integer に変換
     */
    private Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Long) {
            return ((Long) value).intValue();
        }
        return Integer.valueOf(value.toString());
    }

    /**
     * Map から取得した値を BigDecimal に変換
     */
    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        return new BigDecimal(value.toString());
    }

    /**
     * Map から取得した値を LocalDate に変換（java.sql.Date 対応）
     */
    private LocalDate toLocalDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate) {
            return (LocalDate) value;
        }
        if (value instanceof java.sql.Date) {
            return ((java.sql.Date) value).toLocalDate();
        }
        return LocalDate.parse(value.toString());
    }

    /**
     * Map から取得した値を String に変換
     */
    private String toString(Object value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }
}
