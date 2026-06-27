package com.dorm.server.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 办理退住 DTO
 *
 * @author dorm-server
 */
@Data
public class CheckoutDTO {

    /** 退住日期（必填） */
    @NotNull(message = "退居日は必須です")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkoutDate;

    /** 乐观锁版本号（必填） */
    @NotNull(message = "バージョン番号は必須です")
    private Integer version;

    /** 退住备注 */
    private String remark;
}
