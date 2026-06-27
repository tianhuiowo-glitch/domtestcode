package com.dorm.server.service.impl;

import com.dorm.server.constant.RedisKeyConstants;
import com.dorm.server.constant.SystemConstants;
import com.dorm.server.entity.vo.RegionVO;
import com.dorm.server.mapper.RegionMapper;
import com.dorm.server.service.RegionService;
import com.dorm.server.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 地域业务实现类
 *
 * @author dorm-server
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RegionServiceImpl implements RegionService {

    private final RegionMapper regionMapper;
    private final RedisUtil redisUtil;

    @Override
    public List<RegionVO> listRegions() {
        // 优先从缓存获取
        Object cached = redisUtil.get(RedisKeyConstants.REGION_LIST);
        if (cached != null) {
            log.info("[地域列表] 命中缓存");
            @SuppressWarnings("unchecked")
            List<RegionVO> result = (List<RegionVO>) cached;
            return result;
        }

        List<RegionVO> regions = regionMapper.selectAll();
        // 缓存1小时
        redisUtil.set(RedisKeyConstants.REGION_LIST, regions,
                SystemConstants.DICT_CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
        log.info("[地域列表] 查询完成，count={}", regions.size());
        return regions;
    }
}
