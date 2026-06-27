package com.dorm.server.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 设备处理记录实体类（损坏/丢失）
 * 映射 equipment_processes 表（supplement.sql 中新建）
 *
 * @author dorm-server
 */
@Data
public class EquipmentProcess {

    /** 主键ID */
    private Long id;

    /** 设备ID */
    private Integer equipmentId;

    /** 入居记录ID */
    private Integer checkinId;

    /** 处理类型（damaged/lost） */
    private String processType;

    /** 处理说明 */
    private String description;

    /** 处理费用 */
    private BigDecimal cost;

    /** 状态（pending/completed） */
    private String status;

    /** 完成时间 */
    private LocalDateTime completedAt;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 软删除时间 */
    private LocalDateTime deletedAt;
}
