package com.dorm.server.service;

import com.dorm.server.entity.common.PageVO;
import com.dorm.server.entity.dto.CreateRoomDTO;
import com.dorm.server.entity.dto.UpdateRoomDTO;
import com.dorm.server.entity.vo.RoomVO;

import java.util.List;

/**
 * 房间业务接口
 *
 * @author dorm-server
 */
public interface RoomService {

    /**
     * 分页查询房间列表
     *
     * @param dormitoryId 宿舍ID
     * @param page        页码
     * @param pageSize    每页大小
     * @return 分页结果
     */
    PageVO<RoomVO> listRooms(Integer dormitoryId, Integer page, Integer pageSize);

    /**
     * 查询房间详情
     *
     * @param id 房间ID
     * @return 房间 VO
     */
    RoomVO getRoomById(Integer id);

    /**
     * 新增房间
     *
     * @param dto 新增参数
     * @return 新增后的房间 VO
     */
    RoomVO createRoom(CreateRoomDTO dto);

    /**
     * 更新房间
     *
     * @param id  房间ID
     * @param dto 更新参数（含version）
     * @return 更新后的房间 VO
     */
    RoomVO updateRoom(Integer id, UpdateRoomDTO dto);

    /**
     * 删除房间（软删除）
     *
     * @param id 房间ID
     */
    void deleteRoom(Integer id);

    /**
     * 查询空闲房间列表
     *
     * @param dormitoryId 宿舍ID
     * @param date        判定基准日期（yyyy-MM-dd 格式，null 时默认当日）
     * @return 空闲房间列表
     */
    List<RoomVO> getVacantRooms(Integer dormitoryId, String date);
}
