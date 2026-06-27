package com.dorm.server.controller;

import com.dorm.server.entity.common.PageVO;
import com.dorm.server.entity.common.Result;
import com.dorm.server.entity.dto.CheckoutDTO;
import com.dorm.server.entity.dto.CreateCheckinDTO;
import com.dorm.server.entity.vo.CheckinVO;
import com.dorm.server.service.CheckinService;
import com.dorm.server.service.LogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 入住接口 Controller
 * 映射 residence_histories 表，对应前端 /checkins 模块
 *
 * @author dorm-server
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/checkins")
@RequiredArgsConstructor
public class CheckinController {

    private final CheckinService checkinService;
    private final LogService logService;

    /**
     * 分页查询入住记录
     * GET /api/v1/checkins?page=1&pageSize=20&keyword=&status=&dormitoryId=
     */
    @GetMapping
    public Result<PageVO<CheckinVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer dormitoryId) {
        log.info("[API] GET /api/v1/checkins, page={}, status={}", page, status);
        PageVO<CheckinVO> pageVO = checkinService.listCheckins(page, pageSize, keyword, status, dormitoryId);
        return Result.success(pageVO);
    }

    /**
     * 查询入住详情
     * GET /api/v1/checkins/{id}
     */
    @GetMapping("/{id}")
    public Result<CheckinVO> getById(@PathVariable Integer id) {
        log.info("[API] GET /api/v1/checkins/{}", id);
        CheckinVO vo = checkinService.getCheckinById(id);
        return Result.success(vo);
    }

    /**
     * 新增入住记录
     * POST /api/v1/checkins
     */
    @PostMapping
    public Result<CheckinVO> create(@RequestBody @Validated CreateCheckinDTO dto,
                                    HttpServletRequest request) {
        log.info("[API] POST /api/v1/checkins, employeeName={}, roomId={}", dto.getEmployeeName(), dto.getRoomId());
        CheckinVO vo = checkinService.createCheckin(dto);

        // 入居操作ログを非同期記録
        String username = (String) request.getAttribute("username");
        String ip = request.getRemoteAddr();
        logService.asyncLog(username, "checkin", "checkin",
                String.valueOf(vo.getId()), "入居:" + vo.getEmployeeName(), ip);

        return Result.success(vo);
    }

    /**
     * 办理退住
     * POST /api/v1/checkins/{id}/checkout
     */
    @PostMapping("/{id}/checkout")
    public Result<CheckinVO> checkout(@PathVariable Integer id,
                                      @RequestBody @Validated CheckoutDTO dto,
                                      HttpServletRequest request) {
        log.info("[API] POST /api/v1/checkins/{}/checkout, checkoutDate={}", id, dto.getCheckoutDate());
        CheckinVO vo = checkinService.checkout(id, dto);

        // 退寮操作ログを非同期記録
        String username = (String) request.getAttribute("username");
        String ip = request.getRemoteAddr();
        logService.asyncLog(username, "checkout", "checkin",
                String.valueOf(id), "退寮", ip);

        return Result.success(vo);
    }
}
