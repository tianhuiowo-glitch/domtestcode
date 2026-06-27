package com.dorm.server.controller;

import com.dorm.server.entity.common.Result;
import com.dorm.server.entity.dto.CreateResidenceDTO;
import com.dorm.server.entity.dto.UpdateResidenceDTO;
import com.dorm.server.entity.vo.ResidenceVO;
import com.dorm.server.service.LogService;
import com.dorm.server.service.ResidenceChangeLogService;
import com.dorm.server.service.ResidenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 入居履歴接口 Controller（新规格 /residences 接口）
 * 映射 residence_histories 表
 *
 * @author dorm-server
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/residences")
@RequiredArgsConstructor
public class ResidenceController {

    private final ResidenceService residenceService;
    private final LogService logService;
    private final ResidenceChangeLogService residenceChangeLogService;

    /**
     * 查询入居履歴详情
     * GET /api/v1/residences/{id}
     */
    @GetMapping("/{id}")
    public Result<ResidenceVO> getById(@PathVariable Integer id) {
        log.info("[API] GET /api/v1/residences/{}", id);
        ResidenceVO vo = residenceService.getResidenceById(id);
        return Result.success(vo);
    }

    /**
     * 新增入居履歴
     * POST /api/v1/residences
     */
    @PostMapping
    public Result<ResidenceVO> create(@RequestBody @Validated CreateResidenceDTO dto,
                                      HttpServletRequest request) {
        log.info("[API] POST /api/v1/residences, residentName={}", dto.getResidentName());
        ResidenceVO vo = residenceService.createResidence(dto);

        String username = (String) request.getAttribute("username");
        String ip = request.getRemoteAddr();

        // 入居登録操作ログを非同期記録
        logService.asyncLog(username, "create", "checkin",
                String.valueOf(vo.getId()), "入居登録:" + vo.getResidentName(), ip);

        // 入居履歴変更ログを非同期記録
        residenceChangeLogService.recordChange(vo.getId(), "INSERT", username,
                vo.getResidentName(), vo.getDormitoryName(), vo.getRoomName());

        return Result.success(vo);
    }

    /**
     * 更新入居履歴
     * PUT /api/v1/residences/{id}
     */
    @PutMapping("/{id}")
    public Result<ResidenceVO> update(@PathVariable Integer id,
                                      @RequestBody @Validated UpdateResidenceDTO dto,
                                      HttpServletRequest request) {
        log.info("[API] PUT /api/v1/residences/{}", id);
        ResidenceVO vo = residenceService.updateResidence(id, dto);

        String username = (String) request.getAttribute("username");
        String ip = request.getRemoteAddr();

        // 更新操作ログを非同期記録
        logService.asyncLog(username, "update", "checkin",
                String.valueOf(id), "入居更新", ip);

        // 入居履歴変更ログを非同期記録
        residenceChangeLogService.recordChange(id, "UPDATE", username,
                vo.getResidentName(), vo.getDormitoryName(), vo.getRoomName());

        return Result.success(vo);
    }

    /**
     * 删除入居履歴（软删除）
     * DELETE /api/v1/residences/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id, HttpServletRequest request) {
        log.info("[API] DELETE /api/v1/residences/{}", id);

        // 削除前に情報取得（削除後はVOが取得できないため）
        ResidenceVO vo = residenceService.getResidenceById(id);
        residenceService.deleteResidence(id);

        String username = (String) request.getAttribute("username");
        String ip = request.getRemoteAddr();

        // 削除操作ログを非同期記録
        logService.asyncLog(username, "delete", "checkin",
                String.valueOf(id), "入居削除", ip);

        // 入居履歴変更ログを非同期記録
        residenceChangeLogService.recordChange(id, "DELETE", username,
                vo.getResidentName(), vo.getDormitoryName(), vo.getRoomName());

        return Result.success(null);
    }
}
