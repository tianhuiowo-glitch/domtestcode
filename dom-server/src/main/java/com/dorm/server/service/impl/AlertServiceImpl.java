package com.dorm.server.service.impl;

import com.dorm.server.constant.RedisKeyConstants;
import com.dorm.server.constant.SystemConstants;
import com.dorm.server.entity.common.PageVO;
import com.dorm.server.entity.vo.AlertSummaryVO;
import com.dorm.server.entity.vo.LongTermAlertVO;
import com.dorm.server.entity.vo.WithdrawalAlertVO;
import com.dorm.server.mapper.ResidenceHistoryMapper;
import com.dorm.server.service.AlertService;
import com.dorm.server.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 预警业务实现类
 *
 * @author dorm-server
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertServiceImpl implements AlertService {

    private final ResidenceHistoryMapper residenceHistoryMapper;
    private final RedisUtil redisUtil;

    @Override
    public PageVO<LongTermAlertVO> listLongTermAlerts(Integer page, Integer pageSize,
                                                      String alertLevel, Integer minDays) {
        // 根据 alertLevel 确定天数区间 [effectiveMinDays, effectiveMaxDays)，下推到 SQL 层过滤，
        // 避免「先按天数排序分页、再按级别过滤」导致级别过滤在分页之后形同虚设的问题
        Integer effectiveMinDays;
        Integer effectiveMaxDays;
        if (minDays != null) {
            // 调用方显式指定了 minDays，优先级最高，不强加区间上限
            effectiveMinDays = minDays;
            effectiveMaxDays = null;
        } else if (SystemConstants.ALERT_LEVEL_CRITICAL.equals(alertLevel)) {
            effectiveMinDays = SystemConstants.LONG_TERM_CRITICAL_DAYS;
            effectiveMaxDays = null;
        } else if (SystemConstants.ALERT_LEVEL_WARNING.equals(alertLevel)) {
            effectiveMinDays = SystemConstants.LONG_TERM_WARNING_DAYS;
            effectiveMaxDays = SystemConstants.LONG_TERM_CRITICAL_DAYS;
        } else {
            // alertLevel 为空：保持原有默认行为，不限制级别，全部返回
            effectiveMinDays = SystemConstants.LONG_TERM_WARNING_DAYS;
            effectiveMaxDays = null;
        }

        int offset = (page - 1) * pageSize;
        List<LongTermAlertVO> items = residenceHistoryMapper.selectLongTermAlerts(
                effectiveMinDays, effectiveMaxDays, null, offset, pageSize);
        Long total = residenceHistoryMapper.selectLongTermAlertCount(effectiveMinDays, effectiveMaxDays, null);

        // 对每条记录设置 alertLevel 和 thresholdDays（warning/critical，用于前端展示，与过滤逻辑无关）
        for (LongTermAlertVO vo : items) {
            if (vo.getStayDays() != null && vo.getStayDays() >= SystemConstants.LONG_TERM_CRITICAL_DAYS) {
                vo.setAlertLevel(SystemConstants.ALERT_LEVEL_CRITICAL);
                vo.setThresholdDays(SystemConstants.LONG_TERM_CRITICAL_DAYS);
            } else {
                vo.setAlertLevel(SystemConstants.ALERT_LEVEL_WARNING);
                vo.setThresholdDays(SystemConstants.LONG_TERM_WARNING_DAYS);
            }
        }

        log.info("[长期预警] page={}, minDays={}, maxDays={}, total={}", page, effectiveMinDays, effectiveMaxDays, total);
        return PageVO.of(items, total, page, pageSize);
    }

    @Override
    public AlertSummaryVO getAlertSummary() {
        // 尝试从缓存获取
        Object cached = redisUtil.get(RedisKeyConstants.ALERT_SUMMARY);
        if (cached != null) {
            log.info("[预警汇总] 命中缓存");
            return (AlertSummaryVO) cached;
        }

        List<Map<String, Object>> counts = residenceHistoryMapper.selectAlertCounts(
                SystemConstants.LONG_TERM_WARNING_DAYS,
                SystemConstants.LONG_TERM_CRITICAL_DAYS,
                SystemConstants.WITHDRAWAL_ALERT_DAYS
        );

        AlertSummaryVO summary = new AlertSummaryVO();
        summary.setLongTermWarningCount(0);
        summary.setLongTermCriticalCount(0);
        summary.setWithdrawalAlertCount(0);

        // 解析统计结果
        if (counts != null) {
            for (Map<String, Object> row : counts) {
                String type = (String) row.get("type");
                Object cnt = row.get("cnt");
                int count = cnt != null ? ((Number) cnt).intValue() : 0;
                if ("warning".equals(type)) {
                    summary.setLongTermWarningCount(count);
                } else if ("critical".equals(type)) {
                    summary.setLongTermCriticalCount(count);
                } else if ("withdrawal".equals(type)) {
                    summary.setWithdrawalAlertCount(count);
                }
            }
        }

        summary.setTotalAlertCount(
                summary.getLongTermWarningCount()
                        + summary.getLongTermCriticalCount()
                        + summary.getWithdrawalAlertCount()
        );

        // 缓存10分钟
        redisUtil.set(RedisKeyConstants.ALERT_SUMMARY, summary,
                SystemConstants.SUMMARY_CACHE_EXPIRE_SECONDS * 2, TimeUnit.SECONDS);

        return summary;
    }

    @Override
    public List<WithdrawalAlertVO> getWithdrawalAlerts() {
        List<WithdrawalAlertVO> list = residenceHistoryMapper.selectWithdrawalAlerts(
                SystemConstants.WITHDRAWAL_ALERT_DAYS);
        log.info("[退住预警] count={}", list.size());
        return list;
    }
}
