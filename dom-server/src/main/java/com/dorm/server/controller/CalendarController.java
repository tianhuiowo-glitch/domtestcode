package com.dorm.server.controller;

import com.dorm.server.entity.common.Result;
import com.dorm.server.entity.vo.CalendarDataVO;
import com.dorm.server.service.CalendarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 日历接口 Controller
 *
 * @author dorm-server
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    /**
     * 查询指定年月日历数据（入住/退住事件）
     * GET /api/v1/calendar?region_id=&year=2024&month=6&room_filter=all
     *
     * @param regionId   地域ID（region_id，可选）
     * @param year       年份（必填）
     * @param month      月份（必填）
     * @param roomFilter 房间过滤条件：all=全部、vacant=空室、occupied=入居中（可选，默认all）
     * @return 日历数据 VO
     */
    @GetMapping
    public Result<CalendarDataVO> getCalendar(
            @RequestParam(value = "region_id", required = false) Integer regionId,
            @RequestParam Integer year,
            @RequestParam Integer month,
            @RequestParam(value = "room_filter", required = false, defaultValue = "all") String roomFilter) {
        log.info("[API] GET /api/v1/calendar, regionId={}, year={}, month={}, roomFilter={}", regionId, year, month, roomFilter);
        CalendarDataVO vo = calendarService.getCalendarData(regionId, year, month, roomFilter);
        return Result.success(vo);
    }
}
