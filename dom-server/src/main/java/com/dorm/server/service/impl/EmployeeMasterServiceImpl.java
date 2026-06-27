package com.dorm.server.service.impl;

import com.dorm.server.constant.MessageConstants;
import com.dorm.server.entity.vo.EmployeeMasterVO;
import com.dorm.server.exception.BusinessException;
import com.dorm.server.mapper.EmployeeMasterMapper;
import com.dorm.server.mapper.ResidenceHistoryMapper;
import com.dorm.server.service.EmployeeMasterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 社員マスタ業務実装クラス
 *
 * @author dorm-server
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeMasterServiceImpl implements EmployeeMasterService {

    /** 派遣社員D番号のプレフィックス */
    private static final String DISPATCH_ID_PREFIX = "D";

    /** D番号の数値部分の桁数 */
    private static final int DISPATCH_ID_NUM_LENGTH = 6;

    /** D番号採番の初期値（D000001から開始） */
    private static final int DISPATCH_ID_INIT = 1;

    private final EmployeeMasterMapper employeeMasterMapper;
    private final ResidenceHistoryMapper residenceHistoryMapper;

    @Override
    public List<EmployeeMasterVO> searchByKeyword(String keyword) {
        log.info("[社員マスタ検索] keyword={}", keyword);

        List<EmployeeMasterVO> result = employeeMasterMapper.searchByKeyword(keyword);

        // 結果が null の場合は空リストを返す（null返却禁止）
        return result != null ? result : new ArrayList<>(0);
    }

    @Override
    public EmployeeMasterVO getByEmployeeId(String employeeId) {
        if (!StringUtils.hasText(employeeId)) {
            throw new BusinessException(MessageConstants.EMPLOYEE_ID_EMPTY);
        }

        log.info("[社員マスタ詳細] employeeId={}", employeeId);

        EmployeeMasterVO vo = employeeMasterMapper.selectVoByEmployeeId(employeeId);
        if (vo == null) {
            throw new BusinessException(MessageConstants.DATA_NOT_FOUND);
        }
        return vo;
    }

    @Override
    public String getNextDispatchId(String prefix) {
        // プレフィックスが未指定の場合はデフォルト "D" を使用
        String effectivePrefix = (StringUtils.hasText(prefix)) ? prefix : DISPATCH_ID_PREFIX;
        log.info("[採番自動生成] prefix={}", effectivePrefix);

        // residence_histories テーブルから指定プレフィックスの最大値を取得
        String maxId = residenceHistoryMapper.selectMaxDispatchId(effectivePrefix);

        int nextNum;
        if (!StringUtils.hasText(maxId)) {
            // 既存番号なし → プレフィックス+000001 から開始
            nextNum = DISPATCH_ID_INIT;
            log.info("[採番自動生成] 既存番号なし、{}000001 から開始", effectivePrefix);
        } else {
            // 最大番号から数値部分を抽出して +1
            String numPart = maxId.substring(effectivePrefix.length());
            nextNum = Integer.parseInt(numPart) + 1;
            log.info("[採番自動生成] 最大番号={}, 次の番号={}", maxId, nextNum);
        }

        // プレフィックス + 6桁ゼロ埋め形式でフォーマット（例: D000028、S000003）
        String nextId = effectivePrefix + String.format("%0" + DISPATCH_ID_NUM_LENGTH + "d", nextNum);
        log.info("[採番自動生成] 生成された番号={}", nextId);
        return nextId;
    }
}
