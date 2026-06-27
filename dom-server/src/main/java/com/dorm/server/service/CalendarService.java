package com.dorm.server.service;

import com.dorm.server.entity.vo.CalendarDataVO;

/**
 * 日历业务接口
 *
 * @author dorm-server
 */
public interface CalendarService {

    /**
     * 查询指定年月的入住/退住日历数据
     *
     * @param regionId   地域ID（可选，null=全部）
     * @param year       年份
     * @param month      月份
     * @param roomFilter 房间过滤条件：all=全部、vacant=空室、occupied=入居中
     * @return 日历数据 VO
     */
    CalendarDataVO getCalendarData(Integer regionId, Integer year, Integer month, String roomFilter);
}
