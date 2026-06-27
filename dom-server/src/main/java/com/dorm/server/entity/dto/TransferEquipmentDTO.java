package com.dorm.server.entity.dto;

import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 備品転寮リクエスト DTO
 *
 * @author dorm-server
 */
@Data
public class TransferEquipmentDTO {

    /** 転寮先寮ID */
    @NotNull(message = "転寮先寮IDは必須です")
    private Integer targetDormitoryId;
}
