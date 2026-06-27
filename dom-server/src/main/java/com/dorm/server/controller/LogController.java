package com.dorm.server.controller;

import com.dorm.server.entity.common.PageVO;
import com.dorm.server.entity.common.Result;
import com.dorm.server.entity.vo.LogVO;
import com.dorm.server.service.LogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 操作日志接口 Controller
 *
 * @author dorm-server
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/logs")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    /**
     * 分页查询操作日志
     * GET /api/v1/logs?page=1&pageSize=20&username=&action=&resource=&startDate=&endDate=
     */
    @GetMapping
    public Result<PageVO<LogVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String resource,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        log.info("[API] GET /api/v1/logs, page={}, username={}", page, username);
        PageVO<LogVO> pageVO = logService.listLogs(page, pageSize, username,
                action, resource, startDate, endDate);
        return Result.success(pageVO);
    }
}
