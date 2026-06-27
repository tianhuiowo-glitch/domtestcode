package com.dorm.server.mapper;

import com.dorm.server.entity.EquipmentProcess;
import com.dorm.server.entity.vo.EquipmentProcessVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 设备处理记录持久层接口
 *
 * @author dorm-server
 */
public interface EquipmentProcessMapper {

    /**
     * 根据入居记录ID查询设备处理记录列表
     *
     * @param checkinId 入居记录ID
     * @return 处理记录 VO 列表
     */
    List<EquipmentProcessVO> selectByCheckinId(@Param("checkinId") Integer checkinId);

    /**
     * 根据ID查询处理记录 VO
     *
     * @param id 记录ID
     * @return 处理记录 VO
     */
    EquipmentProcessVO selectVoById(@Param("id") Long id);

    /**
     * 根据ID查询处理记录实体
     *
     * @param id 记录ID
     * @return 处理记录实体
     */
    EquipmentProcess selectById(@Param("id") Long id);

    /**
     * 新增设备处理记录
     *
     * @param process 处理记录实体
     * @return 影响行数
     */
    Integer insert(EquipmentProcess process);

    /**
     * 完成设备处理（更新状态为 completed）
     *
     * @param id 记录ID
     * @return 影响行数
     */
    Integer completeProcess(@Param("id") Long id);
}
