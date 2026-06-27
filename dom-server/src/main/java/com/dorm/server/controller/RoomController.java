package com.dorm.server.controller;

import com.dorm.server.entity.common.PageVO;
import com.dorm.server.entity.common.Result;
import com.dorm.server.entity.dto.CreateRoomDTO;
import com.dorm.server.entity.dto.UpdateRoomDTO;
import com.dorm.server.entity.vo.RoomVO;
import com.dorm.server.service.LogService;
import com.dorm.server.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 房间接口 Controller
 *
 * @author dorm-server
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final LogService logService;

    /**
     * 分页查询房间列表
     * GET /api/v1/rooms?dormitoryId=&page=1&pageSize=20
     */
    @GetMapping
    public Result<PageVO<RoomVO>> list(
            @RequestParam(required = false) Integer dormitoryId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("[API] GET /api/v1/rooms, dormitoryId={}, page={}", dormitoryId, page);
        PageVO<RoomVO> pageVO = roomService.listRooms(dormitoryId, page, pageSize);
        return Result.success(pageVO);
    }

    /**
     * 查询空闲房间列表
     * GET /api/v1/rooms/vacant?dormitoryId=&date=yyyy-MM-dd
     * 注意：此路径必须在 /{id} 之前注册，避免路由冲突
     */
    @GetMapping("/vacant")
    public Result<List<RoomVO>> getVacant(
            @RequestParam(required = false) Integer dormitoryId,
            @RequestParam(required = false) String date) {
        log.info("[API] GET /api/v1/rooms/vacant, dormitoryId={}, date={}", dormitoryId, date);
        List<RoomVO> rooms = roomService.getVacantRooms(dormitoryId, date);
        return Result.success(rooms);
    }

    /**
     * 查询房间详情
     * GET /api/v1/rooms/{id}
     */
    @GetMapping("/{id}")
    public Result<RoomVO> getById(@PathVariable Integer id) {
        log.info("[API] GET /api/v1/rooms/{}", id);
        RoomVO vo = roomService.getRoomById(id);
        return Result.success(vo);
    }

    /**
     * 新增房间
     * POST /api/v1/rooms
     */
    @PostMapping
    public Result<RoomVO> create(@RequestBody @Validated CreateRoomDTO dto,
                                 HttpServletRequest request) {
        log.info("[API] POST /api/v1/rooms, dormitoryId={}, name={}", dto.getDormitoryId(), dto.getName());
        RoomVO vo = roomService.createRoom(dto);

        // 新増操作ログを非同期記録
        String username = (String) request.getAttribute("username");
        String ip = request.getRemoteAddr();
        logService.asyncLog(username, "create", "room",
                String.valueOf(vo.getId()), "部屋作成:" + vo.getName(), ip);

        return Result.success(vo);
    }

    /**
     * 更新房间
     * PUT /api/v1/rooms/{id}
     */
    @PutMapping("/{id}")
    public Result<RoomVO> update(@PathVariable Integer id,
                                 @RequestBody @Validated UpdateRoomDTO dto,
                                 HttpServletRequest request) {
        log.info("[API] PUT /api/v1/rooms/{}", id);
        RoomVO vo = roomService.updateRoom(id, dto);

        // 更新操作ログを非同期記録
        String username = (String) request.getAttribute("username");
        String ip = request.getRemoteAddr();
        logService.asyncLog(username, "update", "room",
                String.valueOf(vo.getId()), "部屋更新:" + vo.getName(), ip);

        return Result.success(vo);
    }

    /**
     * 删除房间（软删除）
     * DELETE /api/v1/rooms/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id, HttpServletRequest request) {
        log.info("[API] DELETE /api/v1/rooms/{}", id);
        roomService.deleteRoom(id);

        // 削除操作ログを非同期記録
        String username = (String) request.getAttribute("username");
        String ip = request.getRemoteAddr();
        logService.asyncLog(username, "delete", "room",
                String.valueOf(id), "部屋削除", ip);

        return Result.success(null);
    }
}
