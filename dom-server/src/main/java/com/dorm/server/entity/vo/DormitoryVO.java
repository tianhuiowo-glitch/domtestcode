package com.dorm.server.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 宿舍 VO
 * 返回宿舍详情及关联地域信息
 *
 * @author dorm-server
 */
@Data
public class DormitoryVO {

    /** 宿舍ID */
    private Integer id;

    /** 所属地域ID */
    private Integer regionId;

    /** 所属地域名称 */
    private String regionName;

    /** 宿舍名称 */
    private String name;

    /** 宿舍类型（male/female/mixed） */
    private String dormitoryType;

    /** 地址 */
    private String address;

    /** 日额费用 */
    private BigDecimal dailyRate;

    /** 排序号 */
    private Integer sortOrder;

    /** 乐观锁版本号 */
    private Integer version;

    /** 总房间数 */
    private Integer totalRooms;

    /** 已入住房间数 */
    private Integer occupiedRooms;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
