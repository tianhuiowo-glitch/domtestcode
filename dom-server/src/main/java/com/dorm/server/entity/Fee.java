package com.dorm.server.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 寮費实体类
 * 映射 fees 表（supplement.sql 中新建）
 *
 * @author dorm-server
 */
@Data
public class Fee {

    /** 主键ID */
    private Long id;

    /** 入居记录ID */
    private Integer residenceId;

    /** 社員番号 */
    private String employeeId;

    /** 社員名 */
    private String employeeName;

    /** 宿舍ID */
    private Integer dormitoryId;

    /** 宿舍名 */
    private String dormitoryName;

    /** 房间ID */
    private Integer roomId;

    /** 房间名 */
    private String roomName;

    /** 费用期间开始日 */
    private LocalDate periodStart;

    /** 费用期间结束日 */
    private LocalDate periodEnd;

    /** 滞在日数 */
    private Integer stayDays;

    /** 日额 */
    private BigDecimal dailyRate;

    /** 基本費用 */
    private BigDecimal baseAmount;

    /** 日用品費 */
    private BigDecimal dailySuppliesCost;

    /** 合計金額 */
    private BigDecimal totalAmount;

    /** 状態（pending/confirmed） */
    private String status;

    /** 確定日時 */
    private LocalDateTime confirmedAt;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 软删除时间 */
    private LocalDateTime deletedAt;
}
