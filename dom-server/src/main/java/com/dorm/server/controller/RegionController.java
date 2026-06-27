package com.dorm.server.controller;

import com.dorm.server.entity.common.Result;
import com.dorm.server.entity.vo.RegionVO;
import com.dorm.server.service.RegionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 地域接口 Controller
 *
 * @author dorm-server
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/regions")
@RequiredArgsConstructor
public class RegionController {

    private final RegionService regionService;

    /**
     * 查询所有地域列表
     * GET /api/v1/regions
     */
    @GetMapping
    public Result<List<RegionVO>> list() {
        log.info("[API] GET /api/v1/regions");
        List<RegionVO> regions = regionService.listRegions();
        return Result.success(regions);
    }
}
