package com.dorm.server.entity.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 寮費レコード更新 DTO
 * PUT /api/v1/fees/{id} のリクエストボディ
 * 対象は pending ステータスの費用レコードのみ編集可能
 *
 * @author dorm-server
 */
@Data
public class UpdateFeeDTO {

    /**
     * 費用期間 開始日（必須）
     */
    @NotNull(message = "費用期間開始日は必須です")
    private LocalDate periodStart;

    /**
     * 費用期間 終了日（必須）
     * periodStart より後の日付を指定すること（Service 層でバリデーション）
     */
    @NotNull(message = "費用期間終了日は必須です")
    private LocalDate periodEnd;

    /**
     * 日額（必須、0.01以上）
     */
    @NotNull(message = "日額は必須です")
    @DecimalMin(value = "0.01", message = "日額は0.01以上を指定してください")
    private BigDecimal dailyRate;

}
