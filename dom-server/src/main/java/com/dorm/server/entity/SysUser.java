package com.dorm.server.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户实体类
 * 映射 sys_users 表（supplement.sql 中新建）
 *
 * @author dorm-server
 */
@Data
public class SysUser {

    /** 主键ID */
    private Long id;

    /** 用户名 */
    private String username;

    /** 密码（BCrypt加密） */
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 角色（admin/operator） */
    private String role;

    /** 状态（0:禁用 1:启用） */
    private Integer status;

    /** 最后登录时间 */
    private LocalDateTime lastLoginAt;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 软删除时间 */
    private LocalDateTime deletedAt;
}
