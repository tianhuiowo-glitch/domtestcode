package com.dorm.server.entity.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 添加设备到库存 DTO
 *
 * @author dorm-server
 */
@Data
public class AddToStorageDTO {

    /** 设备ID（必填） */
    @NotNull(message = "備品IDは必須です")
    private Integer equipmentId;

    /** 存放位置 */
    private String storageLocation;

    /** 备注 */
    private String remarks;
}
