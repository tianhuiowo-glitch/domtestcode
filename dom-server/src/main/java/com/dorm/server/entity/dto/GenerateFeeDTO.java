package com.dorm.server.entity.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 寮費月次一括生成 DTO
 *
 * @author dorm-server
 */
@Data
public class GenerateFeeDTO {

    /**
     * 対象年（例：2026）
     */
    @NotNull(message = "対象年月は必須です")
    private Integer year;

    /**
     * 対象月（1〜12）
     */
    @NotNull(message = "対象年月は必須です")
    @Min(value = 1, message = "月は1〜12の範囲で指定してください")
    @Max(value = 12, message = "月は1〜12の範囲で指定してください")
    private Integer month;
}
