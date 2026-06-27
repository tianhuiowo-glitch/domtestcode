package com.dorm.server.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 寮費 VO
 *
 * @author dorm-server
 */
@Data
public class FeeVO {

    /** 费用ID */
    private Long id;

    /** 入居记录ID */
    @JsonProperty("checkinId")
    private Integer residenceId;

    /** 社員番号 */
    private String employeeId;

    /** 社員名 */
    private String employeeName;

    /** 宿舍ID */
    private Integer dormitoryId;

    /** 宿舍名称 */
    private String dormitoryName;

    /** 房间ID */
    private Integer roomId;

    /** 房间名 */
    @JsonProperty("roomNumber")
    private String roomName;

    /** 费用期间开始日 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate periodStart;

    /** 费用期间结束日 */
    @JsonFormat(pattern = "yyyy-MM-dd")
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
    @JsonProperty("amount")
    private BigDecimal totalAmount;

    /** 状態（pending/confirmed） */
    private String status;

    /** 確定日時 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime confirmedAt;

    /** 確定担当者 */
    private String confirmedBy;

    /** 支払日時 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paidAt;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
