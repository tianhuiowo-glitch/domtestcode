package com.dorm.server.entity.vo;

import lombok.Data;

/**
 * 部门 VO
 *
 * @author dorm-server
 */
@Data
public class DepartmentVO {

    /** 部门ID */
    private Integer id;

    /** 部门名称 */
    private String name;

    /** 排序号 */
    private Integer sortOrder;
}
