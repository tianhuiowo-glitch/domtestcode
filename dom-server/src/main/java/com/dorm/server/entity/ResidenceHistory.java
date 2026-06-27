package com.dorm.server.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 入居履歴实体类
 * 映射 residence_histories 表（含 supplement.sql 中新增的字段）
 *
 * @author dorm-server
 */
@Data
public class ResidenceHistory {

    /** 主键ID */
    private Integer id;

    /** 社員番号（supplement.sql 新增） */
    private String employeeId;

    /** 性別（supplement.sql 新增，male/female） */
    private String gender;

    /** 房间ID */
    private Integer roomId;

    /** 所属部门ID */
    private Integer departmentId;

    /** 社員名 */
    private String residentName;

    /** 入住日期 */
    private LocalDate checkInDate;

    /** 退住日期（null 表示当前在住） */
    private LocalDate checkOutDate;

    /** 计划退住日期（supplement.sql 新增） */
    private LocalDate plannedCheckoutDate;

    /** 是否负责人（0:否 1:是） */
    private Boolean isResponsible;

    /** 备注 */
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
