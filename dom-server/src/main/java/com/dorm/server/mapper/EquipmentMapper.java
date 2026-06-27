package com.dorm.server.mapper;

import com.dorm.server.entity.Equipment;
import com.dorm.server.entity.vo.EquipmentVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 设备持久层接口
 *
 * @author dorm-server
 */
public interface EquipmentMapper {

    /**
     * 分页查询设备列表
     *
     * @param dormitoryId 宿舍ID（可选）
     * @param status      设备状态（可选）
     * @param keyword     关键词（设备名/管理番号模糊）
     * @param offset      分页偏移量
     * @param pageSize    每页大小
     * @return 设备 VO 列表
     */
    List<EquipmentVO> selectPageList(@Param("dormitoryId") Integer dormitoryId,
                                     @Param("status") String status,
                                     @Param("keyword") String keyword,
                                     @Param("offset") Integer offset,
                                     @Param("pageSize") Integer pageSize);

    /**
     * 统计分页总数
     */
    Long selectPageCount(@Param("dormitoryId") Integer dormitoryId,
                         @Param("status") String status,
                         @Param("keyword") String keyword);

    /**
     * 根据ID查询设备 VO
     *
     * @param id 设备ID
     * @return 设备 VO
     */
    EquipmentVO selectVoById(@Param("id") Integer id);

    /**
     * 根据ID查询设备实体
     *
     * @param id 设备ID
     * @return 设备实体
     */
    Equipment selectById(@Param("id") Integer id);

    /**
     * 更新设备状态
     *
     * @param id     设备ID
     * @param status 新状态
     * @return 影响行数
     */
    Integer updateStatus(@Param("id") Integer id, @Param("status") String status);

    /**
     * 備品を別の寮に転寮する（dormitory_id を更新、room_id をクリア）
     *
     * @param id                備品ID
     * @param targetDormitoryId 転寮先寮ID
     * @return 影響行数
     */
    Integer transferDormitory(@Param("id") Integer id, @Param("targetDormitoryId") Integer targetDormitoryId);

    /**
     * 備品をソフトデリート（廃棄）
     *
     * @param id 備品ID
     * @return 影響行数
     */
    Integer softDeleteById(@Param("id") Integer id);
}
