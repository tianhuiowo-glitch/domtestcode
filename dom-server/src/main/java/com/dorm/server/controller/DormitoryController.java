package com.dorm.server.controller;

import com.dorm.server.entity.common.PageVO;
import com.dorm.server.entity.common.Result;
import com.dorm.server.entity.dto.CreateDormitoryDTO;
import com.dorm.server.entity.dto.UpdateDormitoryDTO;
import com.dorm.server.entity.dto.UpdateDormitoryTypeDTO;
import com.dorm.server.entity.vo.DormitoryVO;
import com.dorm.server.service.DormitoryService;
import com.dorm.server.service.LogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 宿舍接口 Controller
 *
 * @author dorm-server
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/dormitories")
@RequiredArgsConstructor
public class DormitoryController {

    private final DormitoryService dormitoryService;
    private final LogService logService;

    /**
     * 分页查询宿舍列表
     * GET /api/v1/dormitories?page=1&pageSize=20&keyword=&regionId=
     */
    @GetMapping
    public Result<PageVO<DormitoryVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer regionId) {
        log.info("[API] GET /api/v1/dormitories, page={}, pageSize={}", page, pageSize);
        PageVO<DormitoryVO> pageVO = dormitoryService.listDormitories(page, pageSize, keyword, regionId);
        return Result.success(pageVO);
    }

    /**
     * 查询宿舍详情
     * GET /api/v1/dormitories/{id}
     */
    @GetMapping("/{id}")
    public Result<DormitoryVO> getById(@PathVariable Integer id) {
        log.info("[API] GET /api/v1/dormitories/{}", id);
        DormitoryVO vo = dormitoryService.getDormitoryById(id);
        return Result.success(vo);
    }

    /**
     * 新増宿舍
     * POST /api/v1/dormitories
     */
    @PostMapping
    public Result<DormitoryVO> create(@RequestBody @Validated CreateDormitoryDTO dto,
                                      HttpServletRequest request) {
        log.info("[API] POST /api/v1/dormitories, name={}", dto.getName());
        DormitoryVO vo = dormitoryService.createDormitory(dto);

        // 新增操作ログを非同期記録
        String username = (String) request.getAttribute("username");
        String ip = request.getRemoteAddr();
        logService.asyncLog(username, "create", "dormitory",
                String.valueOf(vo.getId()), "宿舍作成:" + vo.getName(), ip);

        return Result.success(vo);
    }

    /**
     * 更新宿舍
     * PUT /api/v1/dormitories/{id}
     */
    @PutMapping("/{id}")
    public Result<DormitoryVO> update(@PathVariable Integer id,
                                      @RequestBody @Validated UpdateDormitoryDTO dto,
                                      HttpServletRequest request) {
        log.info("[API] PUT /api/v1/dormitories/{}", id);
        DormitoryVO vo = dormitoryService.updateDormitory(id, dto);

        // 更新操作ログを非同期記録
        String username = (String) request.getAttribute("username");
        String ip = request.getRemoteAddr();
        logService.asyncLog(username, "update", "dormitory",
                String.valueOf(vo.getId()), "宿舍更新:" + vo.getName(), ip);

        return Result.success(vo);
    }

    /**
     * 删除宿舍（软删除）
     * DELETE /api/v1/dormitories/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id, HttpServletRequest request) {
        log.info("[API] DELETE /api/v1/dormitories/{}", id);
        dormitoryService.deleteDormitory(id);

        // 削除操作ログを非同期記録
        String username = (String) request.getAttribute("username");
        String ip = request.getRemoteAddr();
        logService.asyncLog(username, "delete", "dormitory",
                String.valueOf(id), "宿舍削除", ip);

        return Result.success(null);
    }

    /**
     * 宿舎タイプを変更（全室空室の場合のみ許可）
     * PUT /api/v1/dormitories/{id}/type
     */
    @PutMapping("/{id}/type")
    public Result<DormitoryVO> updateType(@PathVariable Integer id,
                                          @RequestBody @Validated UpdateDormitoryTypeDTO dto,
                                          HttpServletRequest request) {
        log.info("[API] PUT /api/v1/dormitories/{}/type, dormitoryType={}", id, dto.getDormitoryType());
        DormitoryVO vo = dormitoryService.updateDormitoryType(id, dto);

        // タイプ変更操作ログを非同期記録
        String username = (String) request.getAttribute("username");
        String ip = request.getRemoteAddr();
        logService.asyncLog(username, "update", "dormitory",
                String.valueOf(id), "寮タイプ変更:" + dto.getDormitoryType(), ip);

        return Result.success(vo);
    }
}
