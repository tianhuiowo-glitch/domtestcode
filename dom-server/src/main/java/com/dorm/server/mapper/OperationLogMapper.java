package com.dorm.server.mapper;

import com.dorm.server.entity.OperationLog;
import com.dorm.server.entity.vo.LogVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 操作日志持久层接口
 *
 * @author dorm-server
 */
public interface OperationLogMapper {

    /**
     * 分页查询操作日志
     *
     * @param username  操作用户名（可选）
     * @param action    操作动作（可选）
     * @param resource  操作资源（可选）
     * @param startDate 开始日期字符串（可选）
     * @param endDate   结束日期字符串（可选）
     * @param offset    分页偏移量
     * @param pageSize  每页大小
     * @return 日志 VO 列表
     */
    List<LogVO> selectPageList(@Param("username") String username,
                               @Param("action") String action,
                               @Param("resource") String resource,
                               @Param("startDate") String startDate,
                               @Param("endDate") String endDate,
                               @Param("offset") Integer offset,
                               @Param("pageSize") Integer pageSize);

    /**
     * 统计分页总数
     */
    Long selectPageCount(@Param("username") String username,
                         @Param("action") String action,
                         @Param("resource") String resource,
                         @Param("startDate") String startDate,
                         @Param("endDate") String endDate);

    /**
     * 新增操作日志
     *
     * @param log 日志实体
     * @return 影响行数
     */
    Integer insert(OperationLog log);
}
