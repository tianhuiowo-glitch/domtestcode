package com.dorm.server.mapper;

import com.dorm.server.entity.Region;
import com.dorm.server.entity.vo.RegionVO;

import java.util.List;

/**
 * 地域持久层接口
 *
 * @author dorm-server
 */
public interface RegionMapper {

    /**
     * 查询所有地域列表（按 sort_order 升序）
     *
     * @return 地域 VO 列表
     */
    List<RegionVO> selectAll();
}
