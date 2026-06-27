package com.dorm.server.entity.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 登录请求 DTO
 *
 * @author dorm-server
 */
@Data
public class LoginDTO {

    /** 用户名（必填） */
    @NotBlank(message = "ユーザー名は必須です")
    private String username;

    /** 密码（必填） */
    @NotBlank(message = "パスワードは必須です")
    private String password;
}
