package com.dorm.server.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 宿舍实体类
 * 映射 dormitories 表
 *
 * @author dorm-server
 */
@Data
public class Dormitory {

    /** 主键ID */
    private Integer id;

    /** 所属地域ID */
    private Integer regionId;

    /** 宿舍名称 */
    private String name;

    /** 宿舍类型（male/female/mixed） */
    private String dormitoryType;

    /** 地址 */
    private String address;

    /** 日额费用 */
    private BigDecimal dailyRate;

    /** 排序号 */
    private Integer sortOrder;

    /** 乐观锁版本号 */
    private Integer version;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 软删除时间（null表示未删除） */
    private LocalDateTime deletedAt;
}
