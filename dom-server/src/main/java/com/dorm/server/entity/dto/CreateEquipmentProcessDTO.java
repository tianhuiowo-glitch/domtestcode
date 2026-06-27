package com.dorm.server.entity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 新增设备处理记录 DTO（损坏/丢失）
 *
 * @author dorm-server
 */
@Data
public class CreateEquipmentProcessDTO {

    /** 设备ID（必填） */
    @NotNull(message = "備品IDは必須です")
    private Integer equipmentId;

    /** 入居记录ID */
    private Integer checkinId;

    /** 处理类型（damaged/lost）（必填） */
    @NotBlank(message = "処理タイプは必須です")
    @JsonProperty("issueType")
    private String processType;

    /** 处理说明 */
    private String description;

    /** 处理费用 */
    @JsonProperty("compensation")
    private BigDecimal cost;
}
