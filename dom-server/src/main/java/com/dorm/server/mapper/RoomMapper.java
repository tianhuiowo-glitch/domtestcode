package com.dorm.server.mapper;

import com.dorm.server.entity.Room;
import com.dorm.server.entity.vo.RoomVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 房间持久层接口
 *
 * @author dorm-server
 */
public interface RoomMapper {

    /**
     * 分页查询房间列表
     *
     * @param dormitoryId 宿舍ID（可选）
     * @param offset      分页偏移量
     * @param pageSize    每页大小
     * @return 房间 VO 列表
     */
    List<RoomVO> selectPageList(@Param("dormitoryId") Integer dormitoryId,
                                @Param("offset") Integer offset,
                                @Param("pageSize") Integer pageSize);

    /**
     * 统计分页总数
     *
     * @param dormitoryId 宿舍ID（可选）
     * @return 总记录数
     */
    Long selectPageCount(@Param("dormitoryId") Integer dormitoryId);

    /**
     * 根据ID查询房间 VO（含宿舍名称、入住人数）
     *
     * @param id 房间ID
     * @return 房间 VO
     */
    RoomVO selectVoById(@Param("id") Integer id);

    /**
     * 根据ID查询房间实体
     *
     * @param id 房间ID
     * @return 房间实体
     */
    Room selectById(@Param("id") Integer id);

    /**
     * 查询指定宿舍下的空闲房间列表
     * 空闲=指定日期时点没有有效入住记录（check_in_date <= date AND (check_out_date IS NULL OR check_out_date > date)）
     *
     * @param dormitoryId 宿舍ID
     * @param date        判定基准日期（yyyy-MM-dd 格式）
     * @return 空闲房间 VO 列表
     */
    List<RoomVO> selectVacantRooms(@Param("dormitoryId") Integer dormitoryId,
                                   @Param("date") String date);

    /**
     * 新增房间
     *
     * @param room 房间实体
     * @return 影响行数
     */
    Integer insert(Room room);

    /**
     * 更新房间（含乐观锁校验）
     *
     * @param room 房间实体（含version字段）
     * @return 影响行数（0=版本冲突）
     */
    Integer updateWithVersion(Room room);

    /**
     * 软删除房间
     *
     * @param id 房间ID
     * @return 影响行数
     */
    Integer softDeleteById(@Param("id") Integer id);

    /**
     * 查询房间当前在住人数
     *
     * @param roomId 房间ID
     * @return 在住人数
     */
    Integer countActiveResidents(@Param("roomId") Integer roomId);

    /**
     * 查询宿舍下所有房间（用于空房统计）
     *
     * @param dormitoryId 宿舍ID
     * @return 房间列表
     */
    List<Room> selectByDormitoryId(@Param("dormitoryId") Integer dormitoryId);

    /**
     * 查询宿舍下所有房间 VO（含当前入住人数，用于空房汇总统计）
     * 替代 selectPageList(dormitoryId, 0, Integer.MAX_VALUE)，避免非必要分页参数
     *
     * @param dormitoryId 宿舍ID
     * @return 房间 VO 列表（含 currentOccupancy）
     */
    List<RoomVO> selectVoListByDormitoryId(@Param("dormitoryId") Integer dormitoryId);

    /**
     * カレンダー用：全部屋を寮・地域情報付きで取得（空室含む）
     *
     * @param regionId 地域ID（null=全地域）
     * @return room_id, room_name, dormitory_id, dormitory_name を持つ Map リスト
     */
    List<Map<String, Object>> selectAllRoomsForCalendar(@Param("regionId") Integer regionId);
}
