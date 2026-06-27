package com.dorm.server.mapper;

import com.dorm.server.entity.ResidenceHistory;
import com.dorm.server.entity.vo.CheckinVO;
import com.dorm.server.entity.vo.EmployeeLookupVO;
import com.dorm.server.entity.vo.LongTermAlertVO;
import com.dorm.server.entity.vo.ResidenceVO;
import com.dorm.server.entity.vo.WithdrawalAlertVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 入居履歴持久层接口
 *
 * @author dorm-server
 */
public interface ResidenceHistoryMapper {

    /**
     * 分页查询入住记录列表
     *
     * @param keyword     关键词（社員名/社員番号模糊搜索）
     * @param status      状态（active/checked_out，null=全部）
     * @param dormitoryId 宿舍ID（可选）
     * @param offset      分页偏移量
     * @param pageSize    每页大小
     * @return 入住 VO 列表
     */
    List<CheckinVO> selectPageList(@Param("keyword") String keyword,
                                   @Param("status") String status,
                                   @Param("dormitoryId") Integer dormitoryId,
                                   @Param("offset") Integer offset,
                                   @Param("pageSize") Integer pageSize);

    /**
     * 统计分页总数
     */
    Long selectPageCount(@Param("keyword") String keyword,
                         @Param("status") String status,
                         @Param("dormitoryId") Integer dormitoryId);

    /**
     * 根据ID查询入住 VO（含宿舍、房间、部门信息）
     *
     * @param id 记录ID
     * @return 入住 VO
     */
    CheckinVO selectCheckinVoById(@Param("id") Integer id);

    /**
     * 根据ID查询实体
     *
     * @param id 记录ID
     * @return 入居履歴实体
     */
    ResidenceHistory selectById(@Param("id") Integer id);

    /**
     * 根据ID查询 ResidenceVO
     *
     * @param id 记录ID
     * @return ResidenceVO
     */
    ResidenceVO selectResidenceVoById(@Param("id") Integer id);

    /**
     * 新增入居记录
     *
     * @param history 实体
     * @return 影响行数
     */
    Integer insert(ResidenceHistory history);

    /**
     * 更新入居记录（含乐观锁）
     *
     * @param history 实体（含version）
     * @return 影响行数（0=版本冲突）
     */
    Integer updateWithVersion(ResidenceHistory history);

    /**
     * 办理退住（更新 check_out_date）
     *
     * @param id          记录ID
     * @param checkoutDate 退住日期字符串 yyyy-MM-dd
     * @param remark      备注
     * @param version     乐观锁版本
     * @return 影响行数（0=版本冲突）
     */
    Integer checkout(@Param("id") Integer id,
                     @Param("checkoutDate") String checkoutDate,
                     @Param("remark") String remark,
                     @Param("version") Integer version);

    /**
     * 软删除入居记录
     *
     * @param id 记录ID
     * @return 影响行数
     */
    Integer softDeleteById(@Param("id") Integer id);

    /**
     * 根据社員番号查询最新一条入居记录（用于 employee lookup）
     *
     * @param employeeId 社員番号
     * @return 员工查询 VO
     */
    EmployeeLookupVO selectEmployeeLookupByEmployeeId(@Param("employeeId") String employeeId);

    /**
     * 查询长期入住预警列表（指定天数区间 [minDays, maxDays)）
     *
     * @param minDays  最小天数（含）
     * @param maxDays  最大天数（不含，可为 null 表示不限上限）
     * @param keyword  关键词（可选）
     * @param offset   分页偏移量
     * @param pageSize 每页大小
     * @return 长期预警 VO 列表
     */
    List<LongTermAlertVO> selectLongTermAlerts(@Param("minDays") Integer minDays,
                                               @Param("maxDays") Integer maxDays,
                                               @Param("keyword") String keyword,
                                               @Param("offset") Integer offset,
                                               @Param("pageSize") Integer pageSize);

    /**
     * 统计长期入住预警总数（指定天数区间 [minDays, maxDays)）
     *
     * @param minDays 最小天数（含）
     * @param maxDays 最大天数（不含，可为 null 表示不限上限）
     * @param keyword 关键词（可选）
     * @return 总数
     */
    Long selectLongTermAlertCount(@Param("minDays") Integer minDays,
                                  @Param("maxDays") Integer maxDays,
                                  @Param("keyword") String keyword);

    /**
     * 查询即将退住预警（未来N天内）
     *
     * @param days 提前天数
     * @return 退住预警 VO 列表
     */
    List<WithdrawalAlertVO> selectWithdrawalAlerts(@Param("days") Integer days);

    /**
     * 统计预警汇总数量
     *
     * @param warningDays  warning 阈值天数
     * @param criticalDays critical 阈值天数
     * @param withdrawDays 退住提前天数
     * @return 数量数组 [warningCount, criticalCount, withdrawalCount]（通过 Map 返回）
     */
    List<java.util.Map<String, Object>> selectAlertCounts(@Param("warningDays") Integer warningDays,
                                                           @Param("criticalDays") Integer criticalDays,
                                                           @Param("withdrawDays") Integer withdrawDays);

    /**
     * 查询日历事件（按日期统计入住/退住数量）
     *
     * @param regionId 地域ID（可选）
     * @param year     年份
     * @param month    月份
     * @return 事件统计列表
     */
    List<java.util.Map<String, Object>> selectCalendarEvents(@Param("regionId") Integer regionId,
                                                              @Param("year") Integer year,
                                                              @Param("month") Integer month);

    /**
     * 指定年月に在籍している入居記録を取得（カレンダーグリッド用）
     * 条件：check_in_date <= 月末 AND (check_out_date IS NULL OR check_out_date >= 月初)
     *
     * @param regionId 地域ID（null = 全地域）
     * @param year     年
     * @param month    月
     * @return 在籍者情報リスト（寮ID/名・部屋ID/名・入居者情報を含む）
     */
    List<java.util.Map<String, Object>> selectCalendarResidents(@Param("regionId") Integer regionId,
                                                                 @Param("year") Integer year,
                                                                 @Param("month") Integer month);

    /**
     * 派遣社員D番号の最大値を取得（employee_id LIKE 'D%' の最大値）
     * D番号自動生成に使用。未削除レコードのみ対象。
     *
     * @return 最大D番号文字列（例: D000027）、存在しない場合 null
     */
    String selectMaxDispatchId(@Param("prefix") String prefix);
}
