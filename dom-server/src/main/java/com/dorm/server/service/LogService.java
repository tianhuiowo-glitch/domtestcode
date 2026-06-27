package com.dorm.server.service;

import com.dorm.server.entity.common.PageVO;
import com.dorm.server.entity.vo.LogVO;

/**
 * 操作日志业务接口
 *
 * @author dorm-server
 */
public interface LogService {

    /**
     * 分页查询操作日志
     *
     * @param page      页码
     * @param pageSize  每页大小
     * @param username  操作用户名
     * @param action    操作动作
     * @param resource  操作资源
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 分页结果
     */
    PageVO<LogVO> listLogs(Integer page, Integer pageSize, String username,
                           String action, String resource,
                           String startDate, String endDate);

    /**
     * 异步记录操作日志
     *
     * @param username   操作用户名
     * @param action     操作动作
     * @param resource   操作资源
     * @param resourceId 资源ID
     * @param detail     操作详情
     * @param ipAddress  操作IP
     */
    void asyncLog(String username, String action, String resource,
                  String resourceId, String detail, String ipAddress);
}
