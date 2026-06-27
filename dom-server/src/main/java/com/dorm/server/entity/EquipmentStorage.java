package com.dorm.server.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 设备库存实体类
 * 映射 equipment_storage 表（supplement.sql 中新建）
 *
 * @author dorm-server
 */
@Data
public class EquipmentStorage {

    /** 主键ID */
    private Long id;

    /** 设备ID */
    private Integer equipmentId;

    /** 设备名称 */
    private String equipmentName;

    /** 设备分类 */
    private String category;

    /** 管理番号 */
    private String serialNumber;

    /** 存放位置 */
    private String storageLocation;

    /** 入库时间 */
    private LocalDateTime storedAt;

    /** 备注 */
    private String remarks;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 软删除时间 */
    private LocalDateTime deletedAt;
}
