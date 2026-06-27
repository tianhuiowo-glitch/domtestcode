package com.dorm.server.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 入居履歴 VO（residences 新规格接口用）
 * 映射 residence_histories 表
 *
 * @author dorm-server
 */
@Data
public class ResidenceVO {

    /** 记录ID */
    private Integer id;

    /** 社員番号 */
    private String employeeId;

    /** 性別 */
    private String gender;

    /** 社員名 */
    private String residentName;

    /** 所属部门ID */
    private Integer departmentId;

    /** 所属部门名称 */
    private String departmentName;

    /** 房间ID */
    private Integer roomId;

    /** 房间名 */
    private String roomName;

    /** 宿舍ID */
    private Integer dormitoryId;

    /** 宿舍名 */
    private String dormitoryName;

    /** 入住日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkInDate;

    /** 退住日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkOutDate;

    /** 计划退住日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate plannedCheckoutDate;

    /** 是否负责人 */
    private Boolean isResponsible;

    /** 备注 */
    private String remarks;

    /** 乐观锁版本 */
    private Integer version;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
