package com.dorm.server.service;

import com.dorm.server.entity.common.PageVO;
import com.dorm.server.entity.dto.CalculateFeeDTO;
import com.dorm.server.entity.dto.ConfirmFeeDTO;
import com.dorm.server.entity.dto.GenerateFeeDTO;
import com.dorm.server.entity.dto.UpdateFeeDTO;
import com.dorm.server.entity.vo.FeeVO;
import com.dorm.server.entity.vo.GenerateFeeResultVO;

import java.util.List;

/**
 * 寮費业务接口
 *
 * @author dorm-server
 */
public interface FeeService {

    /**
     * 分页查询费用列表
     *
     * @param page        页码
     * @param pageSize    每页大小
     * @param status      费用状态
     * @param employeeId  社員番号
     * @param dormitoryId 宿舍ID
     * @param periodStart 期间开始日
     * @param periodEnd   期间结束日
     * @return 分页结果
     */
    PageVO<FeeVO> listFees(Integer page, Integer pageSize, String status,
                           String employeeId, Integer dormitoryId,
                           String periodStart, String periodEnd);

    /**
     * 查询费用详情
     *
     * @param id 费用ID
     * @return 费用 VO
     */
    FeeVO getFeeById(Long id);

    /**
     * 计算寮費
     *
     * @param dto 计算参数（入居ID、费用期间）
     * @return 计算后的费用 VO（未保存，待确认）
     */
    FeeVO calculateFee(CalculateFeeDTO dto);

    /**
     * 批量确认费用
     *
     * @param dto 包含 feeIds 列表
     */
    void confirmFees(ConfirmFeeDTO dto);

    /**
     * 寮費月次一括生成
     * 指定年月に在寮していた全入居者の費用レコードをまとめて生成する。
     * 既存レコード（同一入居ID・同一期間）は重複チェックでスキップ。
     *
     * @param dto 対象年月（year / month）
     * @return 生成結果 VO（generated / skipped / total）
     */
    GenerateFeeResultVO generateMonthlyFees(GenerateFeeDTO dto);

    /**
     * 費用レコード編集（pending ステータスのみ）
     * periodEnd は periodStart より後の日付であること。
     * stay_days / base_amount / total_amount は再計算して保存する。
     *
     * @param id  編集対象の費用レコードID
     * @param dto 更新内容（periodStart / periodEnd / dailyRate / dailySuppliesCost）
     * @return 更新後の費用 VO
     */
    FeeVO updateFee(Long id, UpdateFeeDTO dto);

    /**
     * 单条软删除费用记录（仅允许删除 pending 状态的费用）
     *
     * @param id 费用ID
     */
    void deleteFee(Long id);

    /**
     * 批量软删除费用记录（仅允许删除 pending 状态的费用）
     *
     * @param ids 费用ID列表（不能为空）
     */
    void batchDeleteFees(List<Long> ids);
}
