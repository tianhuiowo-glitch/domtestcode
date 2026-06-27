package com.dorm.server.controller;

import com.dorm.server.entity.common.PageVO;
import com.dorm.server.entity.common.Result;
import com.dorm.server.entity.vo.AlertSummaryVO;
import com.dorm.server.entity.vo.LongTermAlertVO;
import com.dorm.server.entity.vo.WithdrawalAlertVO;
import com.dorm.server.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 预警接口 Controller
 *
 * @author dorm-server
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    /**
     * 分页查询长期入住预警
     * GET /api/v1/alerts/long-term?page=1&pageSize=20&alertLevel=&minDays=
     */
    @GetMapping("/long-term")
    public Result<PageVO<LongTermAlertVO>> listLongTerm(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String alertLevel,
            @RequestParam(required = false) Integer minDays) {
        log.info("[API] GET /api/v1/alerts/long-term, page={}, alertLevel={}", page, alertLevel);
        PageVO<LongTermAlertVO> pageVO = alertService.listLongTermAlerts(page, pageSize, alertLevel, minDays);
        return Result.success(pageVO);
    }

    /**
     * 查询预警汇总统计
     * GET /api/v1/alerts/summary
     */
    @GetMapping("/summary")
    public Result<AlertSummaryVO> getSummary() {
        log.info("[API] GET /api/v1/alerts/summary");
        AlertSummaryVO vo = alertService.getAlertSummary();
        return Result.success(vo);
    }

    /**
     * 查询即将退住预警列表（15天内）
     * GET /api/v1/alerts/withdrawal
     */
    @GetMapping("/withdrawal")
    public Result<List<WithdrawalAlertVO>> getWithdrawal() {
        log.info("[API] GET /api/v1/alerts/withdrawal");
        List<WithdrawalAlertVO> list = alertService.getWithdrawalAlerts();
        return Result.success(list);
    }
}
