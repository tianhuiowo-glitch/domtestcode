package com.dorm.server.controller;

import com.dorm.server.entity.common.PageVO;
import com.dorm.server.entity.common.Result;
import com.dorm.server.entity.vo.DepartmentVO;
import com.dorm.server.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 部门接口 Controller
 *
 * @author dorm-server
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    /**
     * 查询所有部门列表
     * GET /api/v1/departments
     */
    @GetMapping
    public Result<List<DepartmentVO>> list() {
        log.info("[API] GET /api/v1/departments");
        List<DepartmentVO> departments = departmentService.listDepartments();
        return Result.success(departments);
    }

    /**
     * ページネーション付き所属一覧
     * GET /api/v1/departments/page
     */
    @GetMapping("/page")
    public Result<PageVO<DepartmentVO>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        log.info("[API] GET /api/v1/departments/page, page={}, pageSize={}, keyword={}", page, pageSize, keyword);
        return Result.success(departmentService.listDepartmentsPage(page, pageSize, keyword));
    }

    /**
     * 所属新規登録
     * POST /api/v1/departments
     */
    @PostMapping
    public Result<DepartmentVO> create(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        Integer sortOrder = body.get("sortOrder") != null
                ? ((Number) body.get("sortOrder")).intValue()
                : 1;
        log.info("[API] POST /api/v1/departments, name={}", name);
        return Result.success(departmentService.createDepartment(name, sortOrder));
    }

    /**
     * 所属更新
     * PUT /api/v1/departments/{id}
     */
    @PutMapping("/{id}")
    public Result<DepartmentVO> update(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        Integer sortOrder = body.get("sortOrder") != null
                ? ((Number) body.get("sortOrder")).intValue()
                : 1;
        log.info("[API] PUT /api/v1/departments/{}, name={}", id, name);
        return Result.success(departmentService.updateDepartment(id, name, sortOrder));
    }

    /**
     * 所属削除（論理削除）
     * DELETE /api/v1/departments/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        log.info("[API] DELETE /api/v1/departments/{}", id);
        departmentService.deleteDepartment(id);
        return Result.success(null);
    }
}
