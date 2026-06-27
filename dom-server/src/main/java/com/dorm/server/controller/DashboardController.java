package com.dorm.server.controller;

import com.dorm.server.entity.common.Result;
import com.dorm.server.entity.vo.DashboardStatsVO;
import com.dorm.server.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ダッシュボード Controller
 *
 * @author dorm-server
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * ダッシュボード統計データ取得
     * GET /api/v1/dashboard/stats
     */
    @GetMapping("/stats")
    public Result<DashboardStatsVO> getStats() {
        log.info("[API] GET /api/v1/dashboard/stats");
        return Result.success(dashboardService.getStats());
    }
}
