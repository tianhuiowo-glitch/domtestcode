package com.dorm.server.controller;

import com.dorm.server.entity.common.Result;
import com.dorm.server.entity.vo.EmployeeLookupVO;
import com.dorm.server.service.CheckinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 员工查询接口 Controller
 * GET /api/v1/employees/lookup - 根据社員番号查询员工信息
 *
 * @author dorm-server
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final CheckinService checkinService;

    /**
     * 员工信息查询（根据社員番号）
     * GET /api/v1/employees/lookup?employeeId=E001
     * 从 residence_histories 历史记录中查找，无记录则返回默认临时值
     *
     * @param employeeId 社員番号
     * @return 员工查询 VO
     */
    @GetMapping("/lookup")
    public Result<EmployeeLookupVO> lookup(@RequestParam String employeeId) {
        log.info("[API] GET /api/v1/employees/lookup, employeeId={}", employeeId);
        EmployeeLookupVO vo = checkinService.lookupEmployee(employeeId);
        return Result.success(vo);
    }
}
