package com.dorm.server.entity.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 新增宿舍 DTO
 *
 * @author dorm-server
 */
@Data
public class CreateDormitoryDTO {

    /** 所属地域ID（必填） */
    @NotNull(message = "地域IDは必須です")
    private Integer regionId;

    /** 宿舍名称（必填） */
    @NotBlank(message = "寮名は必須です")
    private String name;

    /** 宿舍类型（male/female/mixed） */
    private String dormitoryType;

    /** 地址 */
    private String address;

    /** 日额费用（必填） */
    @NotNull(message = "日額は必須です")
    private BigDecimal dailyRate;

    /** 排序号（必填） */
    @NotNull(message = "表示順は必須です")
    private Integer sortOrder;
}
