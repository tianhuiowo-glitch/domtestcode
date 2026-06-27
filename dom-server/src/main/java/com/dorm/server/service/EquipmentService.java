package com.dorm.server.service;

import com.dorm.server.entity.common.PageVO;
import com.dorm.server.entity.dto.AddToStorageDTO;
import com.dorm.server.entity.dto.CreateEquipmentProcessDTO;
import com.dorm.server.entity.vo.EquipmentProcessVO;
import com.dorm.server.entity.vo.EquipmentVO;
import com.dorm.server.entity.vo.StorageItemVO;

import java.util.List;

/**
 * 设备业务接口
 *
 * @author dorm-server
 */
public interface EquipmentService {

    /**
     * 分页查询设备列表
     *
     * @param page        页码
     * @param pageSize    每页大小
     * @param dormitoryId 宿舍ID
     * @param status      设备状态
     * @param keyword     关键词
     * @return 分页结果
     */
    PageVO<EquipmentVO> listEquipment(Integer page, Integer pageSize,
                                      Integer dormitoryId, String status, String keyword);

    /**
     * 查询设备详情
     *
     * @param id 设备ID
     * @return 设备 VO
     */
    EquipmentVO getEquipmentById(Integer id);

    /**
     * 查询入居记录关联的设备处理记录列表
     *
     * @param checkinId 入居记录ID
     * @return 处理记录列表
     */
    List<EquipmentProcessVO> listProcessesByCheckinId(Integer checkinId);

    /**
     * 新增设备处理记录（损坏/丢失）
     *
     * @param dto 新增参数
     * @return 处理记录 VO
     */
    EquipmentProcessVO createProcess(CreateEquipmentProcessDTO dto);

    /**
     * 完成设备处理
     *
     * @param id 处理记录ID
     * @return 更新后的处理记录 VO
     */
    EquipmentProcessVO completeProcess(Long id);

    /**
     * 查询所有库存列表
     *
     * @return 库存列表
     */
    List<StorageItemVO> listStorage();

    /**
     * 将设备添加到库存
     *
     * @param dto 添加参数
     * @return 库存 VO
     */
    StorageItemVO addToStorage(AddToStorageDTO dto);

    /**
     * 備品を別の寮に転寮する
     *
     * @param id                備品ID
     * @param targetDormitoryId 転寮先寮ID（現役寮であること）
     * @return 転寮後の備品 VO
     */
    EquipmentVO transferEquipment(Integer id, Integer targetDormitoryId);

    /**
     * 備品を廃棄する（ソフトデリート）
     *
     * @param id 備品ID
     */
    void discardEquipment(Integer id);
}
