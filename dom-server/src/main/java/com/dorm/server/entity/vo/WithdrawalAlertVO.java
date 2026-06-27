package com.dorm.server.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;

/**
 * 退住预警 VO
 * 查询 check_out_date 在未来15天内的入住记录
 *
 * @author dorm-server
 */
@Data
public class WithdrawalAlertVO {

    /** 入住记录ID */
    private Integer id;

    /** 社員番号 */
    private String employeeId;

    /** 社員名（フロントエンドは residentName で受け取る） */
    @JsonProperty("residentName")
    private String employeeName;

    /** 宿舍名称 */
    private String dormitoryName;

    /** 房号（フロントエンドは roomName で受け取る） */
    @JsonProperty("roomName")
    private String roomNumber;

    /** 入住日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkinDate;

    /** 计划退住日期（フロントエンドは checkOutDate で受け取る） */
    @JsonProperty("checkOutDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate plannedCheckoutDate;

    /** 距离退住剩余天数（フロントエンドは remainingDays で受け取る） */
    @JsonProperty("remainingDays")
    private Integer daysUntilCheckout;

    /** 所属部门名称 */
    private String departmentName;
}
