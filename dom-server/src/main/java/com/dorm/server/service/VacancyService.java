package com.dorm.server.service;

import com.dorm.server.entity.vo.VacancySummaryVO;

import java.util.List;

/**
 * 空房业务接口
 *
 * @author dorm-server
 */
public interface VacancyService {

    /**
     * 查询所有宿舍空房概况汇总
     *
     * @return 各宿舍空房概况列表
     */
    List<VacancySummaryVO> getVacancySummary();
}
