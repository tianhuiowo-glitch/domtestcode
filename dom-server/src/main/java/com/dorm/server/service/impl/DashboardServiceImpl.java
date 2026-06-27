package com.dorm.server.service.impl;

import com.dorm.server.constant.SystemConstants;
import com.dorm.server.entity.vo.DashboardStatsVO;
import com.dorm.server.mapper.DashboardMapper;
import com.dorm.server.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * ダッシュボード業務実装クラス
 *
 * @author dorm-server
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DashboardMapper dashboardMapper;

    @Override
    public DashboardStatsVO getStats() {
        log.info("[ダッシュボード] 統計データ取得開始");

        DashboardStatsVO vo = new DashboardStatsVO();
        vo.setCurrentResidents(dashboardMapper.countActiveResidents());
        vo.setPendingResidents(dashboardMapper.countPendingResidents());
        vo.setVacantRooms(dashboardMapper.countVacantRooms());
        vo.setWithdrawalAlerts(dashboardMapper.countWithdrawalAlerts(14));
        vo.setDuplicateErrors(dashboardMapper.countDuplicateRooms());
        vo.setLongTermAlerts(dashboardMapper.countLongTermAlerts(SystemConstants.LONG_TERM_WARNING_DAYS));

        log.info("[ダッシュボード] 在籍={}, 入居予定={}, 空室={}, 退寮警告={}, 重複={}, 長期警告={}",
                vo.getCurrentResidents(), vo.getPendingResidents(), vo.getVacantRooms(),
                vo.getWithdrawalAlerts(), vo.getDuplicateErrors(), vo.getLongTermAlerts());
        return vo;
    }
}
