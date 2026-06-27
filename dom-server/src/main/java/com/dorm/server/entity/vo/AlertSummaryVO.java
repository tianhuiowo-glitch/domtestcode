package com.dorm.server.entity.vo;

import lombok.Data;

/**
 * 预警汇总 VO
 * 统计各类预警的数量
 *
 * @author dorm-server
 */
@Data
public class AlertSummaryVO {

    /** 长期入住警告数（90~179天） */
    private Integer longTermWarningCount;

    /** 长期入住严重数（180天以上） */
    private Integer longTermCriticalCount;

    /** 即将退住预警数（15天内退住） */
    private Integer withdrawalAlertCount;

    /** 预警总数 */
    private Integer totalAlertCount;
}
