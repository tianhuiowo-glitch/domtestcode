package com.dorm.server.mapper;

import com.dorm.server.entity.Fee;
import com.dorm.server.entity.vo.FeeVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 寮費持久层接口
 *
 * @author dorm-server
 */
public interface FeeMapper {

    /**
     * 分页查询费用列表
     *
     * @param status      状态（pending/confirmed，null=全部）
     * @param employeeId  社員番号（可选）
     * @param dormitoryId 宿舍ID（可选）
     * @param periodStart 期间开始日（可选）
     * @param periodEnd   期间结束日（可选）
     * @param offset      分页偏移量
     * @param pageSize    每页大小
     * @return 费用 VO 列表
     */
    List<FeeVO> selectPageList(@Param("status") String status,
                               @Param("employeeId") String employeeId,
                               @Param("dormitoryId") Integer dormitoryId,
                               @Param("periodStart") String periodStart,
                               @Param("periodEnd") String periodEnd,
                               @Param("offset") Integer offset,
                               @Param("pageSize") Integer pageSize);

    /**
     * 统计分页总数
     */
    Long selectPageCount(@Param("status") String status,
                         @Param("employeeId") String employeeId,
                         @Param("dormitoryId") Integer dormitoryId,
                         @Param("periodStart") String periodStart,
                         @Param("periodEnd") String periodEnd);

    /**
     * 根据ID查询费用 VO
     *
     * @param id 费用ID
     * @return 费用 VO
     */
    FeeVO selectVoById(@Param("id") Long id);

    /**
     * 根据ID查询费用实体
     *
     * @param id 费用ID
     * @return 费用实体
     */
    Fee selectById(@Param("id") Long id);

    /**
     * 新增费用记录
     *
     * @param fee 费用实体
     * @return 影响行数
     */
    Integer insert(Fee fee);

    /**
     * 批量确认费用状态
     *
     * @param feeIds 费用ID列表
     * @return 影响行数
     */
    Integer batchConfirm(@Param("feeIds") List<Long> feeIds);

    /**
     * 指定月に在寮していた入居記録を全件取得（月次一括生成用）
     * 日額は rooms.daily_rate → dormitories.daily_rate の順で COALESCE して取得
     * 返却 Map のキー：id, employee_id, employee_name, room_id, room_name,
     *                  dormitory_id, dormitory_name, check_in_date, check_out_date,
     *                  daily_rate（COALESCE済み）
     *
     * @param monthStart 月初日（例：2026-06-01）
     * @param monthEnd   月末日（例：2026-06-30）
     * @return 在寮者情報リスト（Map 形式）
     */
    List<Map<String, Object>> selectActiveInMonth(@Param("monthStart") LocalDate monthStart,
                                                   @Param("monthEnd") LocalDate monthEnd);

    /**
     * 指定入居記録・指定期間の費用レコード重複チェック
     *
     * @param residenceId 入居記録ID
     * @param periodStart 費用期間開始日
     * @param periodEnd   費用期間終了日
     * @return 重複件数（0=重複なし、>0=重複あり）
     */
    int countByResidenceAndPeriod(@Param("residenceId") Integer residenceId,
                                  @Param("periodStart") LocalDate periodStart,
                                  @Param("periodEnd") LocalDate periodEnd);

    /**
     * 費用レコード更新（pending ステータスのみ対象）
     * period_start / period_end / stay_days / daily_rate / base_amount /
     * daily_supplies_cost / total_amount を上書きし updated_at を更新する
     *
     * @param fee 更新後の費用エンティティ（id 必須）
     * @return 影響行数（0 の場合は対象なし または pending 以外）
     */
    Integer updateById(Fee fee);

    /**
     * 单条软删除费用记录（设置 deleted_at 时间戳）
     *
     * @param id 费用ID
     * @return 影响行数（0 表示记录不存在或已被删除）
     */
    int softDeleteById(@Param("id") Long id);

    /**
     * 批量软删除费用记录（设置 deleted_at 时间戳）
     *
     * @param ids 费用ID列表
     * @return 影响行数
     */
    int batchDelete(@Param("ids") List<Long> ids);
}
