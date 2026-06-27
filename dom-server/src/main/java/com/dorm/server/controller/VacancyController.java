package com.dorm.server.controller;

import com.dorm.server.entity.common.Result;
import com.dorm.server.entity.vo.VacancySummaryVO;
import com.dorm.server.service.VacancyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 空房接口 Controller
 *
 * @author dorm-server
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/vacancies")
@RequiredArgsConstructor
public class VacancyController {

    private final VacancyService vacancyService;

    /**
     * 查询所有宿舍空房概况汇总
     * GET /api/v1/vacancies/summary
     */
    @GetMapping("/summary")
    public Result<List<VacancySummaryVO>> getSummary() {
        log.info("[API] GET /api/v1/vacancies/summary");
        List<VacancySummaryVO> list = vacancyService.getVacancySummary();
        return Result.success(list);
    }
}
