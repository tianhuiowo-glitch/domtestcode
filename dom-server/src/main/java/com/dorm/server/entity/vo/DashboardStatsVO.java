package com.dorm.server.entity.vo;

import lombok.Data;

/**
 * ダッシュボード統計 VO
 *
 * @author dorm-server
 */
@Data
public class DashboardStatsVO {

    /** 現在の入居者数（check_in_date が今日以前、かつ check_out_date が未設定または今日以降） */
    private Integer currentResidents;

    /** 入居予定者数（check_in_date が未来日の予約入居） */
    private Integer pendingResidents;

    /** 空室数（在籍記録のない部屋） */
    private Integer vacantRooms;

    /** 退寮予定14日以内の件数 */
    private Integer withdrawalAlerts;

    /** 同室重複エラー件数（同日に2名以上在籍している部屋数） */
    private Integer duplicateErrors;

    /** 長期滞在警告数（入居日から90日以上経過している在住者数） */
    private Integer longTermAlerts;
}
