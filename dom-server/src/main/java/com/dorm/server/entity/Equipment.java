package com.dorm.server.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 设备实体类
 * 映射 equipment 表（含 supplement.sql 中新增的字段）
 *
 * @author dorm-server
 */
@Data
public class Equipment {

    /** 主键ID */
    private Integer id;

    /** 设备名称（supplement.sql 新增） */
    private String name;

    /** 设备分类（supplement.sql 新增） */
    private String category;

    /** 管理番号（supplement.sql 新增） */
    private String serialNumber;

    /** 状态（supplement.sql 新增，normal/damaged/lost/in_storage） */
    private String status;

    /** 所属宿舍ID（supplement.sql 新增） */
    private Integer dormitoryId;

    /** 入居记录ID（supplement.sql 新增） */
    private Integer checkinId;

    /** 所属房间ID */
    private Integer roomId;

    /** 设备属性 JSON（原始字段） */
    private String attributes;

    /** 乐观锁版本号 */
    private Integer version;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 软删除时间 */
    private LocalDateTime deletedAt;
}
