package com.dorm.server.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 社員マスタ実体クラス
 * 映射 employee_master 表
 *
 * @author dorm-server
 */
@Data
public class EmployeeMaster {

    /** 主键ID */
    private Integer id;

    /** 社員番号（6桁数字 or D+6桁、UNIQUE） */
    private String employeeId;

    /** 氏名 */
    private String name;

    /** 氏名カナ */
    private String nameKana;

    /** 性别（male/female） */
    private String gender;

    /** 所属部門ID */
    private Integer departmentId;

    /** 在職状態（1=在職、0=退職） */
    private Integer status;

    /** 作成日時 */
    private LocalDateTime createdAt;

    /** 更新日時 */
    private LocalDateTime updatedAt;

    /** 論理削除日時（nullは未削除） */
    private LocalDateTime deletedAt;
}
