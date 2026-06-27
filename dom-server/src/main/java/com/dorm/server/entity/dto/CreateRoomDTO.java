package com.dorm.server.entity.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 新增房间 DTO
 *
 * @author dorm-server
 */
@Data
public class CreateRoomDTO {

    /** 所属宿舍ID（必填） */
    @NotNull(message = "寮IDは必須です")
    private Integer dormitoryId;

    /** 房间名/房号（必填） */
    @NotBlank(message = "部屋名は必須です")
    private String name;

    /** 房间容量（必填，最小1） */
    @NotNull(message = "定員は必須です")
    @Min(value = 1, message = "定員は1以上を指定してください")
    private Integer capacity;
}
