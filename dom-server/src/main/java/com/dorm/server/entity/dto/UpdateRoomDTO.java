package com.dorm.server.entity.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 更新房间 DTO
 * 包含乐观锁版本号
 * 注意：不含 dormitoryId，房间不允许跨寮迁移，dormitoryId 由 existing 记录提供
 *
 * @author dorm-server
 */
@Data
public class UpdateRoomDTO {

    /** 房间名/房号（必填） */
    @NotBlank(message = "部屋名は必須です")
    private String name;

    /** 房间容量（必填，最小1） */
    @NotNull(message = "定員は必須です")
    @Min(value = 1, message = "定員は1以上を指定してください")
    private Integer capacity;

    /** 乐观锁版本号（必填） */
    @NotNull(message = "バージョン番号は必須です")
    private Integer version;
}
