package com.dorm.server.mapper;

import com.dorm.server.entity.EquipmentStorage;
import com.dorm.server.entity.vo.StorageItemVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 设备库存持久层接口
 *
 * @author dorm-server
 */
public interface EquipmentStorageMapper {

    /**
     * 查询所有库存列表
     *
     * @return 库存 VO 列表
     */
    List<StorageItemVO> selectAll();

    /**
     * 根据ID查询库存 VO
     *
     * @param id 库存ID
     * @return 库存 VO
     */
    StorageItemVO selectVoById(@Param("id") Long id);

    /**
     * 新增库存记录
     *
     * @param storage 库存实体
     * @return 影响行数
     */
    Integer insert(EquipmentStorage storage);
}
