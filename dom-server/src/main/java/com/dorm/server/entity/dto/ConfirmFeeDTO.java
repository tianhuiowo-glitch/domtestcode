package com.dorm.server.entity.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 批量确认寮費 DTO
 *
 * @author dorm-server
 */
@Data
public class ConfirmFeeDTO {

    /** 需要确认的费用ID列表（必填，不能为空） */
    @NotEmpty(message = "費用IDリストは必須です")
    private List<Long> feeIds;
}
