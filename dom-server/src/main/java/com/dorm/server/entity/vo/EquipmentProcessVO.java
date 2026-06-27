package com.dorm.server.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 設備処理記録 VO（損傷・紛失）
 *
 * @author dorm-server
 */
@Data
public class EquipmentProcessVO {

    private Long id;

    private Integer equipmentId;

    private String equipmentName;

    private Integer checkinId;

    /** 社員番号 */
    private String employeeId;

    /** 社員名 */
    private String employeeName;

    /** 処理タイプ（フロントエンドは issueType で受け取る） */
    @JsonProperty("issueType")
    private String processType;

    private String description;

    /** 処理費用（フロントエンドは compensation で受け取る） */
    @JsonProperty("compensation")
    private BigDecimal cost;

    /** ステータス（フロントエンドは processStatus で受け取る） */
    @JsonProperty("processStatus")
    private String status;

    /** 完了日時（フロントエンドは processedAt で受け取る） */
    @JsonProperty("processedAt")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completedAt;

    /** 処理担当者 */
    private String processedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
