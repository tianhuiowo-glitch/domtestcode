package com.dorm.server.controller;

import com.dorm.server.entity.common.Result;
import com.dorm.server.entity.vo.EmployeeMasterVO;
import com.dorm.server.service.EmployeeMasterService;
import com.dorm.server.service.LogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 社員マスタ接口 Controller
 *
 * @author dorm-server
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/employee-master")
@RequiredArgsConstructor
public class EmployeeMasterController {

    private final EmployeeMasterService employeeMasterService;
    private final LogService logService;

    /**
     * 社員マスタをキーワードで検索（コンボボックス用、最大20件）
     * GET /api/v1/employee-master/search?keyword=
     */
    @GetMapping("/search")
    public Result<List<EmployeeMasterVO>> search(
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        log.info("[API] GET /api/v1/employee-master/search, keyword={}", keyword);

        List<EmployeeMasterVO> list = employeeMasterService.searchByKeyword(keyword);

        // 検索操作ログを非同期記録
        String username = (String) request.getAttribute("username");
        String ip = request.getRemoteAddr();
        logService.asyncLog(username, "search", "employee-master",
                null, "社員マスタ検索: keyword=" + keyword, ip);

        return Result.success(list);
    }

    /**
     * 社員番号で社員マスタを1件取得
     * GET /api/v1/employee-master/{employeeId}
     */
    @GetMapping("/{employeeId}")
    public Result<EmployeeMasterVO> getByEmployeeId(
            @PathVariable String employeeId,
            HttpServletRequest request) {
        log.info("[API] GET /api/v1/employee-master/{}", employeeId);

        EmployeeMasterVO vo = employeeMasterService.getByEmployeeId(employeeId);

        // 詳細取得操作ログを非同期記録
        String username = (String) request.getAttribute("username");
        String ip = request.getRemoteAddr();
        logService.asyncLog(username, "view", "employee-master",
                employeeId, "社員マスタ詳細取得: employeeId=" + employeeId, ip);

        return Result.success(vo);
    }

    /**
     * 次の派遣社員番号を自動生成して返す（プレフィックス対応）
     * D→大連、S→瀋陽、C→CDXT
     * GET /api/v1/employee-master/next-dispatch-id?prefix=D
     */
    @GetMapping("/next-dispatch-id")
    public Result<String> getNextDispatchId(
            @RequestParam(required = false, defaultValue = "D") String prefix,
            HttpServletRequest request) {
        log.info("[API] GET /api/v1/employee-master/next-dispatch-id, prefix={}", prefix);

        String nextDispatchId = employeeMasterService.getNextDispatchId(prefix);

        // 採番操作ログを非同期記録
        String username = (String) request.getAttribute("username");
        String ip = request.getRemoteAddr();
        logService.asyncLog(username, "generate", "employee-master",
                null, "社員番号自動採番: prefix=" + prefix + ", 生成番号=" + nextDispatchId, ip);

        return Result.success(nextDispatchId);
    }
}
