package com.dorm.server.entity.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 宿舎タイプ変更 DTO
 *
 * @author dorm-server
 */
@Data
public class UpdateDormitoryTypeDTO {

    /**
     * 新しい宿舎タイプ（male/female/mixed）
     */
    @NotBlank(message = "宿舎タイプは必須です")
    @Pattern(regexp = "^(male|female|mixed)$", message = "宿舎タイプはmale/female/mixedのいずれかを指定してください")
    private String dormitoryType;
}
