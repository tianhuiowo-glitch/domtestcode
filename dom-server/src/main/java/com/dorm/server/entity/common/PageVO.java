package com.dorm.server.entity.common;

import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * 统一分页响应 VO
 * 适配 Element-Plus 分页组件，固定 items/total/page/pageSize 结构
 *
 * @param <T> 列表数据类型
 * @author dorm-server
 */
@Data
public class PageVO<T> {

    /** 当前页数据列表 */
    private List<T> items;

    /** 总记录数 */
    private Long total;

    /** 当前页码 */
    private Integer page;

    /** 每页大小 */
    private Integer pageSize;

    /**
     * 构建分页结果
     *
     * @param items    当前页数据
     * @param total    总记录数
     * @param page     当前页码
     * @param pageSize 每页大小
     * @param <T>      数据类型
     * @return 分页 VO
     */
    public static <T> PageVO<T> of(List<T> items, Long total, Integer page, Integer pageSize) {
        PageVO<T> vo = new PageVO<>();
        vo.setItems(items != null ? items : Collections.emptyList());
        vo.setTotal(total != null ? total : 0L);
        vo.setPage(page);
        vo.setPageSize(pageSize);
        return vo;
    }

    /**
     * 构建空分页结果
     *
     * @param page     当前页码
     * @param pageSize 每页大小
     * @param <T>      数据类型
     * @return 空分页 VO
     */
    public static <T> PageVO<T> empty(Integer page, Integer pageSize) {
        return of(Collections.emptyList(), 0L, page, pageSize);
    }
}
