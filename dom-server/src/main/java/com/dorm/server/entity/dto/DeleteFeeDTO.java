package com.dorm.server.entity.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 批量删除寮費 DTO
 *
 * @author dorm-server
 */
@Data
public class DeleteFeeDTO {

    /** 需要删除的费用ID列表（必填，不能为空） */
    @NotEmpty(message = "削除する費用IDリストは必須です")
    private List<Long> ids;
}
