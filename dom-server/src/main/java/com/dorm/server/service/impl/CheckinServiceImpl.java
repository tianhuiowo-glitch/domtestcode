package com.dorm.server.service.impl;

import com.dorm.server.constant.MessageConstants;
import com.dorm.server.constant.SystemConstants;
import com.dorm.server.entity.ResidenceHistory;
import com.dorm.server.entity.Room;
import com.dorm.server.entity.common.PageVO;
import com.dorm.server.entity.dto.CheckoutDTO;
import com.dorm.server.entity.dto.CreateCheckinDTO;
import com.dorm.server.entity.vo.CheckinVO;
import com.dorm.server.entity.vo.EmployeeLookupVO;
import com.dorm.server.exception.BusinessException;
import com.dorm.server.mapper.DormitoryMapper;
import com.dorm.server.mapper.ResidenceHistoryMapper;
import com.dorm.server.mapper.RoomMapper;
import com.dorm.server.service.CheckinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 入住业务实现类
 *
 * @author dorm-server
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CheckinServiceImpl implements CheckinService {

    private final ResidenceHistoryMapper residenceHistoryMapper;
    private final RoomMapper roomMapper;
    private final DormitoryMapper dormitoryMapper;

    @Override
    public PageVO<CheckinVO> listCheckins(Integer page, Integer pageSize, String keyword,
                                          String status, Integer dormitoryId) {
        int offset = (page - 1) * pageSize;
        List<CheckinVO> items = residenceHistoryMapper.selectPageList(keyword, status, dormitoryId, offset, pageSize);
        Long total = residenceHistoryMapper.selectPageCount(keyword, status, dormitoryId);

        log.info("[入住列表] page={}, status={}, total={}", page, status, total);
        return PageVO.of(items, total, page, pageSize);
    }

    @Override
    public CheckinVO getCheckinById(Integer id) {
        CheckinVO vo = residenceHistoryMapper.selectCheckinVoById(id);
        if (vo == null) {
            throw new BusinessException(MessageConstants.CHECKIN_NOT_FOUND);
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CheckinVO createCheckin(CreateCheckinDTO dto) {
        log.info("[新增入住] roomId={}, employeeName={}", dto.getRoomId(), dto.getEmployeeName());

        // 校验房间存在
        Room room = roomMapper.selectById(dto.getRoomId());
        if (room == null) {
            throw new BusinessException(MessageConstants.ROOM_NOT_FOUND);
        }

        // 校验房间容量（当前入住人数 < 容量）
        Integer currentOccupancy = roomMapper.countActiveResidents(dto.getRoomId());
        if (currentOccupancy != null && currentOccupancy >= room.getCapacity()) {
            throw new BusinessException(MessageConstants.ROOM_IS_FULL);
        }

        // 性別制約チェック：宿舎タイプと入居者性別の整合性を確認
        String dormitoryType = dormitoryMapper.selectDormitoryTypeByRoomId(dto.getRoomId());
        if (dormitoryType != null) {
            String gender = dto.getGender();
            if (SystemConstants.DORM_TYPE_MALE.equals(dormitoryType)
                    && SystemConstants.GENDER_FEMALE.equals(gender)) {
                // 男性寮に女性は入居不可
                throw new BusinessException(MessageConstants.GENDER_MISMATCH_MALE_DORM);
            }
            if (SystemConstants.DORM_TYPE_FEMALE.equals(dormitoryType)
                    && SystemConstants.GENDER_MALE.equals(gender)) {
                // 女性寮に男性は入居不可
                throw new BusinessException(MessageConstants.GENDER_MISMATCH_FEMALE_DORM);
            }
            // mixed の場合は性別制限なし
        }

        // 构建入居记录
        ResidenceHistory history = new ResidenceHistory();
        history.setEmployeeId(dto.getEmployeeId());
        history.setResidentName(dto.getEmployeeName());
        history.setGender(dto.getGender());
        history.setDepartmentId(dto.getDepartmentId());
        history.setRoomId(dto.getRoomId());
        history.setCheckInDate(dto.getCheckinDate());
        history.setPlannedCheckoutDate(dto.getPlannedCheckoutDate());
        history.setIsResponsible(dto.getIsResponsible() != null ? dto.getIsResponsible() : Boolean.FALSE);
        history.setRemarks(dto.getRemark());
        history.setVersion(1);

        residenceHistoryMapper.insert(history);

        return residenceHistoryMapper.selectCheckinVoById(history.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CheckinVO checkout(Integer id, CheckoutDTO dto) {
        log.info("[办理退住] id={}, checkoutDate={}", id, dto.getCheckoutDate());

        // 查询入住记录
        ResidenceHistory history = residenceHistoryMapper.selectById(id);
        if (history == null) {
            throw new BusinessException(MessageConstants.CHECKIN_NOT_FOUND);
        }

        // 校验未退住
        if (history.getCheckOutDate() != null) {
            throw new BusinessException(MessageConstants.ALREADY_CHECKED_OUT);
        }

        // 校验退住日期不早于入住日期
        if (dto.getCheckoutDate().isBefore(history.getCheckInDate())) {
            throw new BusinessException(MessageConstants.CHECKOUT_DATE_INVALID);
        }

        // 执行退住（乐观锁）
        String checkoutDateStr = dto.getCheckoutDate().toString();
        int affected = residenceHistoryMapper.checkout(id, checkoutDateStr, dto.getRemark(), dto.getVersion());
        if (affected == 0) {
            throw new BusinessException(MessageConstants.VERSION_CONFLICT);
        }

        return residenceHistoryMapper.selectCheckinVoById(id);
    }

    @Override
    public EmployeeLookupVO lookupEmployee(String employeeId) {
        if (!StringUtils.hasText(employeeId)) {
            throw new BusinessException(MessageConstants.EMPLOYEE_ID_EMPTY);
        }

        // 从历史记录中查询
        EmployeeLookupVO vo = residenceHistoryMapper.selectEmployeeLookupByEmployeeId(employeeId);

        if (vo == null) {
            // 无历史记录，返回默认临时数据
            vo = new EmployeeLookupVO();
            vo.setEmployeeId(employeeId);
            vo.setEmployeeName(employeeId);
            vo.setGender(SystemConstants.GENDER_MALE);
            log.info("[员工查询] employeeId={} 无历史记录，返回临时默认值", employeeId);
        }

        return vo;
    }
}
