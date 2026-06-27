package com.dorm.server.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 计算寮費 DTO
 *
 * @author dorm-server
 */
@Data
public class CalculateFeeDTO {

    /** 入居记录ID（必填） */
    @NotNull(message = "入居記録IDは必須です")
    @JsonProperty("checkinId")
    private Integer residenceId;

    /** 日额（必填） */
    @NotNull(message = "日額は必須です")
    private BigDecimal dailyRate;

    /** 费用期间开始日（必填） */
    @NotNull(message = "費用期間開始日は必須です")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate periodStart;

    /** 费用期间结束日（必填） */
    @NotNull(message = "費用期間終了日は必須です")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate periodEnd;
}
