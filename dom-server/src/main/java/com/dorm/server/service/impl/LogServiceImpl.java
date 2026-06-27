package com.dorm.server.service.impl;

import com.dorm.server.constant.SystemConstants;
import com.dorm.server.entity.OperationLog;
import com.dorm.server.entity.common.PageVO;
import com.dorm.server.entity.vo.LogVO;
import com.dorm.server.mapper.OperationLogMapper;
import com.dorm.server.service.LogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作日志业务实现类
 *
 * @author dorm-server
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogServiceImpl implements LogService {

    private final OperationLogMapper operationLogMapper;

    @Override
    public PageVO<LogVO> listLogs(Integer page, Integer pageSize, String username,
                                  String action, String resource,
                                  String startDate, String endDate) {
        int offset = (page - 1) * pageSize;
        List<LogVO> items = operationLogMapper.selectPageList(
                username, action, resource, startDate, endDate, offset, pageSize);
        Long total = operationLogMapper.selectPageCount(
                username, action, resource, startDate, endDate);

        log.info("[日志列表] page={}, username={}, total={}", page, username, total);
        return PageVO.of(items, total, page, pageSize);
    }

    /**
     * 异步记录操作日志，不影响主业务流程
     */
    @Async
    @Override
    public void asyncLog(String username, String action, String resource,
                         String resourceId, String detail, String ipAddress) {
        try {
            OperationLog logEntity = new OperationLog();
            logEntity.setUsername(username);
            logEntity.setAction(action);
            logEntity.setResource(resource);
            logEntity.setResourceId(resourceId);
            logEntity.setDetail(detail);
            logEntity.setIpAddress(ipAddress);
            logEntity.setStatus(SystemConstants.LOG_STATUS_SUCCESS);
            logEntity.setOperatedAt(LocalDateTime.now());

            operationLogMapper.insert(logEntity);
            log.info("[操作日志] user={}, action={}, resource={}", username, action, resource);
        } catch (Exception e) {
            // 日志记录失败不影响主业务
            log.warn("[操作日志记录失败] {}", e.getMessage());
        }
    }
}
