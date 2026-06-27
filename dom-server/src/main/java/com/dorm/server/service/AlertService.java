package com.dorm.server.service;

import com.dorm.server.entity.common.PageVO;
import com.dorm.server.entity.vo.AlertSummaryVO;
import com.dorm.server.entity.vo.LongTermAlertVO;
import com.dorm.server.entity.vo.WithdrawalAlertVO;

import java.util.List;

/**
 * 预警业务接口
 *
 * @author dorm-server
 */
public interface AlertService {

    /**
     * 分页查询长期入住预警列表
     *
     * @param page       页码
     * @param pageSize   每页大小
     * @param alertLevel 预警级别（warning/critical，null=全部）
     * @param minDays    最小在住天数（null=使用默认阈值90天）
     * @return 分页结果
     */
    PageVO<LongTermAlertVO> listLongTermAlerts(Integer page, Integer pageSize,
                                               String alertLevel, Integer minDays);

    /**
     * 查询预警汇总统计
     *
     * @return 预警汇总 VO
     */
    AlertSummaryVO getAlertSummary();

    /**
     * 查询即将退住预警列表（15天内）
     *
     * @return 退住预警列表
     */
    List<WithdrawalAlertVO> getWithdrawalAlerts();
}
