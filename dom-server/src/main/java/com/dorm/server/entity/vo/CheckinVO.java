package com.dorm.server.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 入住记录 VO
 * 映射 residence_histories 表，包含宿舍/房间关联信息和计算字段
 *
 * @author dorm-server
 */
@Data
public class CheckinVO {

    /** 入住记录ID */
    private Integer id;

    /** 社員番号 */
    private String employeeId;

    /** 社員名（resident_name） */
    private String employeeName;

    /** 性別（male/female） */
    private String gender;

    /** 所属部门ID */
    private Integer departmentId;

    /** 所属部门名称 */
    private String departmentName;

    /** 宿舍ID（通过 room 关联） */
    private Integer dormitoryId;

    /** 宿舍名称 */
    private String dormitoryName;

    /** 房间ID */
    private Integer roomId;

    /** 房号（rooms.name） */
    private String roomNumber;

    /** 入住日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkinDate;

    /** 退住日期（null 表示当前在住） */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkoutDate;

    /** 计划退住日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate plannedCheckoutDate;

    /** 在住天数（DATEDIFF 计算字段） */
    private Integer stayDays;

    /**
     * 状态：active=在住，checked_out=已退住
     * 由 check_out_date IS NULL 判断
     */
    private String status;

    /** 是否负责人 */
    private Boolean isResponsible;

    /** 备注（remarks） */
    private String remark;

    /** 乐观锁版本号 */
    private Integer version;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
