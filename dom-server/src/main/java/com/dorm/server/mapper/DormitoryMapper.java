package com.dorm.server.mapper;

import com.dorm.server.entity.Dormitory;
import com.dorm.server.entity.vo.DormitoryVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 宿舍持久层接口
 *
 * @author dorm-server
 */
public interface DormitoryMapper {

    /**
     * 分页查询宿舍列表（含地域名称、房间统计）
     *
     * @param keyword  关键词（宿舍名、地址模糊搜索）
     * @param regionId 地域ID（可选）
     * @param offset   分页偏移量
     * @param pageSize 每页大小
     * @return 宿舍 VO 列表
     */
    List<DormitoryVO> selectPageList(@Param("keyword") String keyword,
                                     @Param("regionId") Integer regionId,
                                     @Param("offset") Integer offset,
                                     @Param("pageSize") Integer pageSize);

    /**
     * 统计分页查询总数
     *
     * @param keyword  关键词
     * @param regionId 地域ID（可选）
     * @return 总记录数
     */
    Long selectPageCount(@Param("keyword") String keyword,
                         @Param("regionId") Integer regionId);

    /**
     * 根据ID查询宿舍 VO（含地域名称）
     *
     * @param id 宿舍ID
     * @return 宿舍 VO
     */
    DormitoryVO selectVoById(@Param("id") Integer id);

    /**
     * 根据ID查询宿舍实体
     *
     * @param id 宿舍ID
     * @return 宿舍实体
     */
    Dormitory selectById(@Param("id") Integer id);

    /**
     * 新增宿舍
     *
     * @param dormitory 宿舍实体
     * @return 影响行数
     */
    Integer insert(Dormitory dormitory);

    /**
     * 更新宿舍（含乐观锁校验）
     *
     * @param dormitory 宿舍实体（含version字段）
     * @return 影响行数（0=版本冲突）
     */
    Integer updateWithVersion(Dormitory dormitory);

    /**
     * 软删除宿舍
     *
     * @param id 宿舍ID
     * @return 影响行数
     */
    Integer softDeleteById(@Param("id") Integer id);

    /**
     * 查询该宿舍下的房间数量（含已删除）
     *
     * @param dormitoryId 宿舍ID
     * @return 房间数
     */
    Integer countRoomsByDormitoryId(@Param("dormitoryId") Integer dormitoryId);

    /**
     * 查询所有未删除宿舍（用于空房汇总）
     *
     * @return 宿舍列表
     */
    List<DormitoryVO> selectAllForVacancy();

    /**
     * 房间IDから宿舎タイプを取得（性別制約チェック用）
     *
     * @param roomId 房间ID
     * @return 宿舎タイプ（male/female/mixed）、存在しない場合 null
     */
    String selectDormitoryTypeByRoomId(@Param("roomId") Integer roomId);

    /**
     * 指定宿舎IDの全房间における在住者数をカウント（空室チェック用）
     * check_out_date IS NULL の未削除レコード数を返す
     *
     * @param dormitoryId 宿舎ID
     * @return 在住者総数
     */
    Integer countActiveResidentsByDormitoryId(@Param("dormitoryId") Integer dormitoryId);

    /**
     * 宿舎タイプを更新
     *
     * @param id            宿舎ID
     * @param dormitoryType 新しい宿舎タイプ
     * @return 影响行数
     */
    Integer updateDormitoryType(@Param("id") Integer id, @Param("dormitoryType") String dormitoryType);

    /**
     * 指定宿舎IDの配下全房間を一括ソフトデリート（宿舎削除時の連鎖削除用）
     *
     * @param dormitoryId 宿舎ID
     * @return 影响行数
     */
    Integer softDeleteRoomsByDormitoryId(@Param("dormitoryId") Integer dormitoryId);
}
