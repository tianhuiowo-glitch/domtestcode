package com.dorm.server.entity.vo;

import lombok.Data;

/**
 * 员工查询 VO
 * 用于 /employees/lookup 接口，从 residence_histories 查询员工信息
 * 若无历史记录则返回临时默认值
 *
 * @author dorm-server
 */
@Data
public class EmployeeLookupVO {

    /** 社員番号 */
    private String employeeId;

    /** 社員名 */
    private String employeeName;

    /** 性別（male/female） */
    private String gender;

    /** 所属部门ID */
    private Integer departmentId;

    /** 所属部门名称 */
    private String departmentName;
}
