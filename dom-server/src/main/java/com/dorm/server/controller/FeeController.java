package com.dorm.server.controller;

import com.dorm.server.entity.common.PageVO;
import com.dorm.server.entity.common.Result;
import com.dorm.server.entity.dto.CalculateFeeDTO;
import com.dorm.server.entity.dto.ConfirmFeeDTO;
import com.dorm.server.entity.dto.DeleteFeeDTO;
import com.dorm.server.entity.dto.GenerateFeeDTO;
import com.dorm.server.entity.dto.UpdateFeeDTO;
import com.dorm.server.entity.vo.FeeVO;
import com.dorm.server.entity.vo.GenerateFeeResultVO;
import com.dorm.server.service.FeeService;
import com.dorm.server.service.LogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 寮費接口 Controller
 *
 * @author dorm-server
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/fees")
@RequiredArgsConstructor
public class FeeController {

    private final FeeService feeService;
    private final LogService logService;

    /**
     * 分页查询费用列表
     * GET /api/v1/fees?page=1&pageSize=20&status=&employeeId=&dormitoryId=&periodStart=&periodEnd=
     */
    @GetMapping
    public Result<PageVO<FeeVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String employeeId,
            @RequestParam(required = false) Integer dormitoryId,
            @RequestParam(required = false) String periodStart,
            @RequestParam(required = false) String periodEnd) {
        log.info("[API] GET /api/v1/fees, page={}, status={}", page, status);
        PageVO<FeeVO> pageVO = feeService.listFees(page, pageSize, status, employeeId,
                dormitoryId, periodStart, periodEnd);
        return Result.success(pageVO);
    }

    /**
     * 查询费用详情
     * GET /api/v1/fees/{id}
     */
    @GetMapping("/{id}")
    public Result<FeeVO> getById(@PathVariable Long id) {
        log.info("[API] GET /api/v1/fees/{}", id);
        FeeVO vo = feeService.getFeeById(id);
        return Result.success(vo);
    }

    /**
     * 计算寮費
     * POST /api/v1/fees/calculate
     */
    @PostMapping("/calculate")
    public Result<FeeVO> calculate(@RequestBody @Validated CalculateFeeDTO dto) {
        log.info("[API] POST /api/v1/fees/calculate, residenceId={}", dto.getResidenceId());
        FeeVO vo = feeService.calculateFee(dto);
        return Result.success(vo);
    }

    /**
     * 批量确認費用
     * POST /api/v1/fees/confirm
     */
    @PostMapping("/confirm")
    public Result<Void> confirm(@RequestBody @Validated ConfirmFeeDTO dto,
                                HttpServletRequest request) {
        log.info("[API] POST /api/v1/fees/confirm, count={}", dto.getFeeIds().size());
        feeService.confirmFees(dto);

        // 費用確定操作ログを非同期記録
        String username = (String) request.getAttribute("username");
        String ip = request.getRemoteAddr();
        logService.asyncLog(username, "confirm", "fee",
                null, "寮費確定（件数:" + dto.getFeeIds().size() + "）", ip);

        return Result.success(null);
    }

    /**
     * 寮費月次一括生成
     * POST /api/v1/fees/generate
     *
     * @param dto     対象年月（year / month）
     * @param request HTTP リクエスト（操作ログ用）
     * @return 生成結果 VO（generated / skipped / total）
     */
    @PostMapping("/generate")
    public Result<GenerateFeeResultVO> generate(@RequestBody @Validated GenerateFeeDTO dto,
                                                HttpServletRequest request) {
        log.info("[API] POST /api/v1/fees/generate, year={}, month={}", dto.getYear(), dto.getMonth());

        GenerateFeeResultVO result = feeService.generateMonthlyFees(dto);

        // 月次一括生成操作ログを非同期記録
        String username = (String) request.getAttribute("username");
        String ip = request.getRemoteAddr();
        logService.asyncLog(username, "generate", "fee", null,
                "寮費月次一括生成（" + dto.getYear() + "年" + dto.getMonth() + "月、"
                        + "生成:" + result.getGenerated() + "件、スキップ:" + result.getSkipped() + "件）",
                ip);

        return Result.success(result);
    }

    /**
     * 費用レコード編集（pending ステータスのみ）
     * PUT /api/v1/fees/{id}
     *
     * @param id      編集対象の費用レコードID
     * @param dto     更新内容（periodStart / periodEnd / dailyRate）
     * @param request HTTP リクエスト（操作ログ用）
     * @return 更新後の費用 VO
     */
    @PutMapping("/{id}")
    public Result<FeeVO> update(@PathVariable Long id,
                                @RequestBody @Validated UpdateFeeDTO dto,
                                HttpServletRequest request) {
        log.info("[API] PUT /api/v1/fees/{}", id);

        FeeVO vo = feeService.updateFee(id, dto);

        // 費用編集操作ログを非同期記録
        String username = (String) request.getAttribute("username");
        String ip = request.getRemoteAddr();
        logService.asyncLog(username, "update", "fee", String.valueOf(id),
                "費用編集（period: " + dto.getPeriodStart() + " ~ " + dto.getPeriodEnd() + "）", ip);

        return Result.success(vo);
    }

    /**
     * 单条软删除费用记录（仅允许删除 pending 状态的费用）
     * DELETE /api/v1/fees/{id}
     *
     * @param id      费用ID
     * @param request HTTP 请求（操作日志用）
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        log.info("[API] DELETE /api/v1/fees/{}", id);

        feeService.deleteFee(id);

        // 费用删除操作日志（非同期记录）
        String username = (String) request.getAttribute("username");
        String ip = request.getRemoteAddr();
        logService.asyncLog(username, "delete", "fee", String.valueOf(id), "費用削除（単件）", ip);

        return Result.success(null);
    }

    /**
     * 批量软删除费用记录（仅允许删除 pending 状态的费用）
     * DELETE /api/v1/fees/batch
     *
     * @param dto     包含 ids 列表的请求体
     * @param request HTTP 请求（操作日志用）
     * @return 操作结果
     */
    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody @Validated DeleteFeeDTO dto,
                                    HttpServletRequest request) {
        log.info("[API] DELETE /api/v1/fees/batch, count={}", dto.getIds().size());

        feeService.batchDeleteFees(dto.getIds());

        // 批量费用删除操作日志（非同期记录）
        String username = (String) request.getAttribute("username");
        String ip = request.getRemoteAddr();
        logService.asyncLog(username, "delete", "fee", null,
                "費用一括削除（件数:" + dto.getIds().size() + "）", ip);

        return Result.success(null);
    }
}
