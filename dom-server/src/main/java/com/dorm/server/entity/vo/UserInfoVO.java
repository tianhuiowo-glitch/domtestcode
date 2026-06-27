package com.dorm.server.entity.vo;

import lombok.Data;

/**
 * 用户信息 VO
 * 登录成功后返回给前端的用户基本信息
 *
 * @author dorm-server
 */
@Data
public class UserInfoVO {

    /** 用户ID */
    private Long id;

    /** 用户名 */
    private String username;

    /** 真实姓名 */
    private String realName;

    /** 角色 */
    private String role;
}
