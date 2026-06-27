package com.dorm.server.entity.vo;

import lombok.Data;

/**
 * 地域 VO
 *
 * @author dorm-server
 */
@Data
public class RegionVO {

    /** 地域ID */
    private Integer id;

    /** 地域名称 */
    private String name;

    /** 排序号 */
    private Integer sortOrder;
}
