package com.dorm.server.entity.vo;

import lombok.Data;

/**
 * 社員マスタ響応 VO
 * 主にフロントエンドのコンボボックス検索に使用
 *
 * @author dorm-server
 */
@Data
public class EmployeeMasterVO {

    /** 社員番号 */
    private String employeeId;

    /** 氏名 */
    private String name;

    /** 氏名カナ */
    private String nameKana;

    /** 性别（male/female） */
    private String gender;

    /** 所属部門ID */
    private Integer departmentId;

    /** 所属部門名 */
    private String departmentName;
}
