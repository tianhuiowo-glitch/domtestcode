package com.dorm.server.entity.vo;

import lombok.Data;

/**
 * 登录响应 VO
 * 包含 JWT Token 和用户信息
 *
 * @author dorm-server
 */
@Data
public class LoginVO {

    /** JWT Token，前端后续请求需携带在 Authorization: Bearer {token} 头中 */
    private String token;

    /** 用户基本信息 */
    private UserInfoVO userInfo;
}
