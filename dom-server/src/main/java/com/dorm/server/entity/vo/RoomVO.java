package com.dorm.server.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 房间 VO
 * 返回房间详情及关联宿舍信息
 *
 * @author dorm-server
 */
@Data
public class RoomVO {

    /** 房间ID */
    private Integer id;

    /** 所属宿舍ID */
    private Integer dormitoryId;

    /** 所属宿舍名称 */
    private String dormitoryName;

    /** 房间名/房号 */
    private String name;

    /** 房间容量 */
    private Integer capacity;

    /** 当前入住人数 */
    private Integer currentOccupancy;

    /** 乐观锁版本号 */
    private Integer version;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
