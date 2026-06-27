package com.dorm.server.service;

import com.dorm.server.entity.vo.DashboardStatsVO;

/**
 * ダッシュボード業務インターフェース
 *
 * @author dorm-server
 */
public interface DashboardService {

    /**
     * ダッシュボード統計データ取得
     *
     * @return 統計 VO
     */
    DashboardStatsVO getStats();
}
