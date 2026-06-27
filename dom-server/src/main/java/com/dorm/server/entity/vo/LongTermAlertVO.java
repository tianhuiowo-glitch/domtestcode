package com.dorm.server.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

/**
 * 长期入住预警 VO
 * 查询入住超过阈值天数的记录（90天=warning，180天=critical）
 *
 * @author dorm-server
 */
@Data
public class LongTermAlertVO {

    /** 入住记录ID */
    private Integer id;

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

    /** 房号 */
    private String roomNumber;

    /** 入住日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkinDate;

    /** 在住天数 */
    private Integer stayDays;

    /**
     * 预警级别：warning（90天以上）/ critical（180天以上）
     */
    private String alertLevel;

    /** 所属部门名称 */
    private String departmentName;

    /** アラートのしきい値日数（warning=90, critical=180） */
    private Integer thresholdDays;
}
