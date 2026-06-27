package com.dorm.server.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 新增入居履歴 DTO（residences 新规格接口）
 * 映射到 residence_histories 表
 *
 * @author dorm-server
 */
@Data
public class CreateResidenceDTO {

    /** 社員番号 */
    private String employeeId;

    /** 性別（male/female） */
    private String gender;

    /** 社員名（必填） */
    @NotBlank(message = "社員名は必須です")
    private String residentName;

    /** 所属部门ID（必填） */
    @NotNull(message = "所属IDは必須です")
    private Integer departmentId;

    /** 房间ID（必填） */
    @NotNull(message = "部屋IDは必須です")
    private Integer roomId;

    /** 入住日期（必填） */
    @NotNull(message = "入居日は必須です")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkInDate;

    /** 实际退住日期（新建时可选填） */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkOutDate;

    /** 计划退住日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate plannedCheckoutDate;

    /** 是否负责人 */
    private Boolean isResponsible;

    /** 备注 */
    private String remarks;
}
