package com.dorm.server.entity.vo;

import lombok.Data;

/**
 * 空房概況 VO
 * 聚合每个宿舍的总房间数、已入住数和空房数
 *
 * @author dorm-server
 */
@Data
public class VacancySummaryVO {

    /** 宿舍ID */
    private Integer dormitoryId;

    /** 宿舍名称 */
    private String dormitoryName;

    /** 地域名称 */
    private String regionName;

    /** 总房间数 */
    private Integer totalRooms;

    /** 已入住房间数（至少有一个 active 入住记录） */
    private Integer occupiedRooms;

    /** 空房数 */
    private Integer vacantRooms;

    /** 入住率（百分比，保留一位小数） */
    private Double occupancyRate;

    /** 宿舍タイプ（male/female/mixed） */
    private String dormitoryType;

    /** メンテナンス中部屋数（現状テーブルにメンテナンス概念なし、0 固定） */
    private Integer maintenanceRooms;

    /** 空室率（0〜1 の小数、フロントエンド用。例: 0.4 = 40% 空室） */
    private Double vacancyRate;
}
