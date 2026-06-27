package com.dorm.server.service;

import com.dorm.server.entity.vo.RegionVO;

import java.util.List;

/**
 * 地域业务接口
 *
 * @author dorm-server
 */
public interface RegionService {

    /**
     * 查询所有地域列表（按排序号升序）
     *
     * @return 地域列表
     */
    List<RegionVO> listRegions();
}
