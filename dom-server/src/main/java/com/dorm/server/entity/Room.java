package com.dorm.server.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 房间实体类
 * 映射 rooms 表
 *
 * @author dorm-server
 */
@Data
public class Room {

    /** 主键ID */
    private Integer id;

    /** 所属宿舍ID */
    private Integer dormitoryId;

    /** 房间名/房号 */
    private String name;

    /** 房间容量 */
    private Integer capacity;

    /** 每日住宿费率（日額、NULL時は宿舍の daily_rate にフォールバック） */
    private BigDecimal dailyRate;

    /** 備考 */
    private String remarks;

    /** 乐观锁版本号 */
    private Integer version;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 软删除时间 */
    private LocalDateTime deletedAt;
}
