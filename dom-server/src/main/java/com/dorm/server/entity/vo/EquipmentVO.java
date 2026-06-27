package com.dorm.server.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 设备 VO
 *
 * @author dorm-server
 */
@Data
public class EquipmentVO {

    /** 设备ID */
    private Integer id;

    /** 设备名称 */
    private String name;

    /** 设备分类 */
    private String category;

    /** 管理番号 */
    private String serialNumber;

    /** 状态（normal/damaged/lost/in_storage） */
    private String status;

    /** 所属宿舍ID */
    private Integer dormitoryId;

    /** 所属宿舍名称 */
    private String dormitoryName;

    /** 所属寮の削除日時（null=現役寮, non-null=削除済み寮） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dormitoryDeletedAt;

    /** 所属房间ID */
    private Integer roomId;

    /** 所属房间名（フロントエンドは roomNumber で受け取る） */
    @JsonProperty("roomNumber")
    private String roomName;

    /** 入居记录ID */
    private Integer checkinId;

    /** 设备属性（JSON 扩展字段） */
    private Object attributes;

    /** 乐观锁版本号 */
    private Integer version;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
