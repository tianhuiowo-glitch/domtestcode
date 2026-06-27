package com.dorm.server.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作ログ VO
 *
 * @author dorm-server
 */
@Data
public class LogVO {

    private Long id;

    private String username;

    private String action;

    private String resource;

    /** リソースID（フロントエンドは number | null として受け取る） */
    private Long resourceId;

    private String detail;

    private String ipAddress;

    private String status;

    /** 操作日時（フロントエンドは createdAt で受け取る） */
    @JsonProperty("createdAt")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operatedAt;
}
