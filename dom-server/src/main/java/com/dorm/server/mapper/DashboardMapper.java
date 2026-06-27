package com.dorm.server.mapper;

import org.apache.ibatis.annotations.Param;

/**
 * ダッシュボード集計用 Mapper
 *
 * @author dorm-server
 */
public interface DashboardMapper {

    /** 現在の在籍者数（check_in_date が今日以前、かつ check_out_date が未設定または今日以降） */
    Integer countActiveResidents();

    /** 入居予定者数（check_in_date が未来日、かつ check_out_date が未設定または今日以降） */
    Integer countPendingResidents();

    /** 空室数（在籍記録のない部屋） */
    Integer countVacantRooms();

    /**
     * 退寮予定 N 日以内の件数
     *
     * @param days 閾値（日数）
     */
    Integer countWithdrawalAlerts(@Param("days") int days);

    /** 同日に2名以上在籍している部屋の数（重複エラー件数） */
    Integer countDuplicateRooms();

    /**
     * 長期滞在警告件数（入居中かつ入居日から minDays 日以上経過している在住者数）
     *
     * @param minDays 閾値（日数）
     */
    Integer countLongTermAlerts(@Param("minDays") Integer minDays);
}
