package com.dorm.server.controller;

import com.dorm.server.entity.common.PageVO;
import com.dorm.server.entity.common.Result;
import com.dorm.server.entity.dto.AddToStorageDTO;
import com.dorm.server.entity.dto.CreateEquipmentProcessDTO;
import com.dorm.server.entity.dto.TransferEquipmentDTO;
import com.dorm.server.entity.vo.EquipmentProcessVO;
import com.dorm.server.entity.vo.EquipmentVO;
import com.dorm.server.entity.vo.StorageItemVO;
import com.dorm.server.service.EquipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 设备接口 Controller
 *
 * @author dorm-server
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/equipment")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;

    /**
     * 分页查询设备列表
     * GET /api/v1/equipment?page=1&pageSize=20&dormitoryId=&status=&keyword=
     */
    @GetMapping
    public Result<PageVO<EquipmentVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Integer dormitoryId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        log.info("[API] GET /api/v1/equipment, page={}, dormitoryId={}", page, dormitoryId);
        PageVO<EquipmentVO> pageVO = equipmentService.listEquipment(page, pageSize, dormitoryId, status, keyword);
        return Result.success(pageVO);
    }

    /**
     * 查询设备详情
     * GET /api/v1/equipment/{id}
     */
    @GetMapping("/{id}")
    public Result<EquipmentVO> getById(@PathVariable Integer id) {
        log.info("[API] GET /api/v1/equipment/{}", id);
        EquipmentVO vo = equipmentService.getEquipmentById(id);
        return Result.success(vo);
    }

    /**
     * 查询入居记录关联的设备处理记录
     * GET /api/v1/equipment/processes?checkinId=
     */
    @GetMapping("/processes")
    public Result<List<EquipmentProcessVO>> listProcesses(
            @RequestParam(required = false) Integer checkinId) {
        log.info("[API] GET /api/v1/equipment/processes, checkinId={}", checkinId);
        List<EquipmentProcessVO> list = equipmentService.listProcessesByCheckinId(checkinId);
        return Result.success(list);
    }

    /**
     * 新增设备处理记录
     * POST /api/v1/equipment/processes
     */
    @PostMapping("/processes")
    public Result<EquipmentProcessVO> createProcess(
            @RequestBody @Validated CreateEquipmentProcessDTO dto) {
        log.info("[API] POST /api/v1/equipment/processes, equipmentId={}", dto.getEquipmentId());
        EquipmentProcessVO vo = equipmentService.createProcess(dto);
        return Result.success(vo);
    }

    /**
     * 完成设备处理
     * POST /api/v1/equipment/processes/{id}/complete
     */
    @PostMapping("/processes/{id}/complete")
    public Result<EquipmentProcessVO> completeProcess(@PathVariable Long id) {
        log.info("[API] POST /api/v1/equipment/processes/{}/complete", id);
        EquipmentProcessVO vo = equipmentService.completeProcess(id);
        return Result.success(vo);
    }

    /**
     * 查询库存列表
     * GET /api/v1/equipment/storage
     */
    @GetMapping("/storage")
    public Result<List<StorageItemVO>> listStorage() {
        log.info("[API] GET /api/v1/equipment/storage");
        List<StorageItemVO> list = equipmentService.listStorage();
        return Result.success(list);
    }

    /**
     * 添加设备到库存
     * POST /api/v1/equipment/storage
     */
    @PostMapping("/storage")
    public Result<StorageItemVO> addToStorage(@RequestBody @Validated AddToStorageDTO dto) {
        log.info("[API] POST /api/v1/equipment/storage, equipmentId={}", dto.getEquipmentId());
        StorageItemVO vo = equipmentService.addToStorage(dto);
        return Result.success(vo);
    }

    /**
     * 備品転寮
     * PUT /api/v1/equipment/{id}/transfer
     */
    @PutMapping("/{id}/transfer")
    public Result<EquipmentVO> transfer(@PathVariable Integer id,
                                        @RequestBody @Validated TransferEquipmentDTO dto) {
        log.info("[API] PUT /api/v1/equipment/{}/transfer, targetDormitoryId={}", id, dto.getTargetDormitoryId());
        EquipmentVO vo = equipmentService.transferEquipment(id, dto.getTargetDormitoryId());
        return Result.success(vo);
    }

    /**
     * 備品廃棄（ソフトデリート）
     * DELETE /api/v1/equipment/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> discard(@PathVariable Integer id) {
        log.info("[API] DELETE /api/v1/equipment/{}", id);
        equipmentService.discardEquipment(id);
        return Result.success(null);
    }
}
