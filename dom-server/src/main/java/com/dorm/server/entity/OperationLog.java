package com.dorm.server.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体类
 * 映射 operation_logs 表（supplement.sql 中新建）
 *
 * @author dorm-server
 */
@Data
public class OperationLog {

    /** 主键ID */
    private Long id;

    /** 操作用户名 */
    private String username;

    /** 操作动作（CREATE/UPDATE/DELETE/LOGIN/LOGOUT等） */
    private String action;

    /** 操作资源（dormitory/room/checkin等） */
    private String resource;

    /** 资源ID */
    private String resourceId;

    /** 操作详情（JSON字符串） */
    private String detail;

    /** 操作IP */
    private String ipAddress;

    /** 操作结果（success/fail） */
    private String status;

    /** 操作时间 */
    private LocalDateTime operatedAt;
}
