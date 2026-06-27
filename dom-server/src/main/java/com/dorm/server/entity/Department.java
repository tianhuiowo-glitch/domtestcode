package com.dorm.server.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 部门实体类
 * 映射 departments 表
 *
 * @author dorm-server
 */
@Data
public class Department {

    /** 主键ID */
    private Integer id;

    /** 部门名称 */
    private String name;

    /** 排序号 */
    private Integer sortOrder;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 软删除时间 */
    private LocalDateTime deletedAt;
}
