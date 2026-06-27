package com.dorm.server.service.impl;

import com.dorm.server.constant.MessageConstants;
import com.dorm.server.entity.ResidenceHistory;
import com.dorm.server.entity.dto.CreateResidenceDTO;
import com.dorm.server.entity.dto.UpdateResidenceDTO;
import com.dorm.server.entity.vo.ResidenceVO;
import com.dorm.server.exception.BusinessException;
import com.dorm.server.mapper.ResidenceHistoryMapper;
import com.dorm.server.service.ResidenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 入居履歴业务实现类（新规格 /residences 接口）
 *
 * @author dorm-server
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResidenceServiceImpl implements ResidenceService {

    private final ResidenceHistoryMapper residenceHistoryMapper;

    @Override
    public ResidenceVO getResidenceById(Integer id) {
        ResidenceVO vo = residenceHistoryMapper.selectResidenceVoById(id);
        if (vo == null) {
            throw new BusinessException(MessageConstants.CHECKIN_NOT_FOUND);
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResidenceVO createResidence(CreateResidenceDTO dto) {
        log.info("[新增入居] residentName={}, roomId={}", dto.getResidentName(), dto.getRoomId());

        ResidenceHistory history = new ResidenceHistory();
        history.setEmployeeId(dto.getEmployeeId());
        history.setGender(dto.getGender());
        history.setResidentName(dto.getResidentName());
        history.setDepartmentId(dto.getDepartmentId());
        history.setRoomId(dto.getRoomId());
        history.setCheckInDate(dto.getCheckInDate());
        history.setCheckOutDate(dto.getCheckOutDate());
        history.setPlannedCheckoutDate(dto.getPlannedCheckoutDate());
        history.setIsResponsible(dto.getIsResponsible() != null ? dto.getIsResponsible() : Boolean.FALSE);
        history.setRemarks(dto.getRemarks());
        history.setVersion(1);

        residenceHistoryMapper.insert(history);
        return residenceHistoryMapper.selectResidenceVoById(history.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResidenceVO updateResidence(Integer id, UpdateResidenceDTO dto) {
        log.info("[更新入居] id={}, version={}", id, dto.getVersion());

        ResidenceHistory existing = residenceHistoryMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(MessageConstants.CHECKIN_NOT_FOUND);
        }

        ResidenceHistory history = new ResidenceHistory();
        history.setId(id);
        // 前端未发送字段使用 existing 记录的值作为回退，防止数据被清空
        history.setEmployeeId(dto.getEmployeeId() != null ? dto.getEmployeeId() : existing.getEmployeeId());
        history.setGender(dto.getGender() != null ? dto.getGender() : existing.getGender());
        history.setResidentName(dto.getResidentName());
        history.setDepartmentId(dto.getDepartmentId());
        history.setRoomId(dto.getRoomId());
        history.setCheckInDate(dto.getCheckInDate());
        history.setCheckOutDate(dto.getCheckOutDate());
        history.setPlannedCheckoutDate(dto.getPlannedCheckoutDate() != null ? dto.getPlannedCheckoutDate() : existing.getPlannedCheckoutDate());
        history.setIsResponsible(dto.getIsResponsible());
        history.setRemarks(dto.getRemarks());
        history.setVersion(dto.getVersion());

        // 退寮日不得早于入寮日
        if (dto.getCheckOutDate() != null && dto.getCheckOutDate().isBefore(dto.getCheckInDate())) {
            throw new BusinessException(MessageConstants.CHECKOUT_DATE_INVALID);
        }

        int affected = residenceHistoryMapper.updateWithVersion(history);
        if (affected == 0) {
            throw new BusinessException(MessageConstants.VERSION_CONFLICT);
        }

        return residenceHistoryMapper.selectResidenceVoById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteResidence(Integer id) {
        log.info("[删除入居] id={}", id);

        ResidenceHistory existing = residenceHistoryMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(MessageConstants.CHECKIN_NOT_FOUND);
        }

        residenceHistoryMapper.softDeleteById(id);
    }
}
