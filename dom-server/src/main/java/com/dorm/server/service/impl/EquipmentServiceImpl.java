package com.dorm.server.service.impl;

import com.dorm.server.constant.MessageConstants;
import com.dorm.server.constant.SystemConstants;
import com.dorm.server.entity.Equipment;
import com.dorm.server.entity.EquipmentProcess;
import com.dorm.server.entity.EquipmentStorage;
import com.dorm.server.entity.common.PageVO;
import com.dorm.server.entity.dto.AddToStorageDTO;
import com.dorm.server.entity.dto.CreateEquipmentProcessDTO;
import com.dorm.server.entity.vo.EquipmentProcessVO;
import com.dorm.server.entity.vo.EquipmentVO;
import com.dorm.server.entity.vo.StorageItemVO;
import com.dorm.server.exception.BusinessException;
import com.dorm.server.mapper.DormitoryMapper;
import com.dorm.server.mapper.EquipmentMapper;
import com.dorm.server.mapper.EquipmentProcessMapper;
import com.dorm.server.mapper.EquipmentStorageMapper;
import com.dorm.server.service.EquipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 设备业务实现类
 *
 * @author dorm-server
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EquipmentServiceImpl implements EquipmentService {

    private final EquipmentMapper equipmentMapper;
    private final EquipmentProcessMapper processMapper;
    private final EquipmentStorageMapper storageMapper;
    private final DormitoryMapper dormitoryMapper;

    @Override
    public PageVO<EquipmentVO> listEquipment(Integer page, Integer pageSize,
                                             Integer dormitoryId, String status, String keyword) {
        int offset = (page - 1) * pageSize;
        List<EquipmentVO> items = equipmentMapper.selectPageList(dormitoryId, status, keyword, offset, pageSize);
        Long total = equipmentMapper.selectPageCount(dormitoryId, status, keyword);

        log.info("[设备列表] page={}, dormitoryId={}, total={}", page, dormitoryId, total);
        return PageVO.of(items, total, page, pageSize);
    }

    @Override
    public EquipmentVO getEquipmentById(Integer id) {
        EquipmentVO vo = equipmentMapper.selectVoById(id);
        if (vo == null) {
            throw new BusinessException(MessageConstants.EQUIPMENT_NOT_FOUND);
        }
        return vo;
    }

    @Override
    public List<EquipmentProcessVO> listProcessesByCheckinId(Integer checkinId) {
        return processMapper.selectByCheckinId(checkinId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EquipmentProcessVO createProcess(CreateEquipmentProcessDTO dto) {
        log.info("[新增设备处理] equipmentId={}, type={}", dto.getEquipmentId(), dto.getProcessType());

        // 校验设备存在
        Equipment equipment = equipmentMapper.selectById(dto.getEquipmentId());
        if (equipment == null) {
            throw new BusinessException(MessageConstants.EQUIPMENT_NOT_FOUND);
        }

        EquipmentProcess process = new EquipmentProcess();
        process.setEquipmentId(dto.getEquipmentId());
        process.setCheckinId(dto.getCheckinId());
        process.setProcessType(dto.getProcessType());
        process.setDescription(dto.getDescription());
        process.setCost(dto.getCost());
        process.setStatus(SystemConstants.PROCESS_STATUS_PENDING);

        processMapper.insert(process);

        // 更新设备状态
        String newStatus = SystemConstants.EQUIPMENT_STATUS_DAMAGED.equals(dto.getProcessType())
                ? SystemConstants.EQUIPMENT_STATUS_DAMAGED
                : SystemConstants.EQUIPMENT_STATUS_LOST;
        equipmentMapper.updateStatus(dto.getEquipmentId(), newStatus);

        return processMapper.selectVoById(process.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EquipmentProcessVO completeProcess(Long id) {
        log.info("[完成设备处理] id={}", id);

        EquipmentProcess process = processMapper.selectById(id);
        if (process == null) {
            throw new BusinessException(MessageConstants.EQUIPMENT_PROCESS_NOT_FOUND);
        }

        processMapper.completeProcess(id);

        // 处理完成后将设备状态恢复正常
        equipmentMapper.updateStatus(process.getEquipmentId(), SystemConstants.EQUIPMENT_STATUS_NORMAL);

        return processMapper.selectVoById(id);
    }

    @Override
    public List<StorageItemVO> listStorage() {
        return storageMapper.selectAll();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StorageItemVO addToStorage(AddToStorageDTO dto) {
        log.info("[添加库存] equipmentId={}", dto.getEquipmentId());

        Equipment equipment = equipmentMapper.selectById(dto.getEquipmentId());
        if (equipment == null) {
            throw new BusinessException(MessageConstants.EQUIPMENT_NOT_FOUND);
        }

        EquipmentStorage storage = new EquipmentStorage();
        storage.setEquipmentId(dto.getEquipmentId());
        storage.setEquipmentName(equipment.getName());
        storage.setCategory(equipment.getCategory());
        storage.setSerialNumber(equipment.getSerialNumber());
        storage.setStorageLocation(dto.getStorageLocation());
        storage.setStoredAt(LocalDateTime.now());
        storage.setRemarks(dto.getRemarks());

        storageMapper.insert(storage);

        // 更新设备状态为库存中
        equipmentMapper.updateStatus(dto.getEquipmentId(), SystemConstants.EQUIPMENT_STATUS_IN_STORAGE);

        return storageMapper.selectVoById(storage.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EquipmentVO transferEquipment(Integer id, Integer targetDormitoryId) {
        log.info("[備品転寮] id={}, targetDormitoryId={}", id, targetDormitoryId);

        Equipment equipment = equipmentMapper.selectById(id);
        if (equipment == null) {
            throw new BusinessException(MessageConstants.EQUIPMENT_NOT_FOUND);
        }

        // 転寮先が現役寮か確認（selectById は deleted_at IS NULL のみ返す）
        com.dorm.server.entity.Dormitory targetDorm = dormitoryMapper.selectById(targetDormitoryId);
        if (targetDorm == null) {
            throw new BusinessException(MessageConstants.DORMITORY_NOT_FOUND);
        }

        equipmentMapper.transferDormitory(id, targetDormitoryId);
        return equipmentMapper.selectVoById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void discardEquipment(Integer id) {
        log.info("[備品廃棄] id={}", id);

        Equipment equipment = equipmentMapper.selectById(id);
        if (equipment == null) {
            throw new BusinessException(MessageConstants.EQUIPMENT_NOT_FOUND);
        }

        equipmentMapper.softDeleteById(id);
    }
}
