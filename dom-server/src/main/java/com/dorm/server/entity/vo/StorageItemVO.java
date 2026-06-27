package com.dorm.server.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 設備在庫 VO
 *
 * @author dorm-server
 */
@Data
public class StorageItemVO {

    private Long id;

    private Integer equipmentId;

    private String equipmentName;

    private String category;

    private String serialNumber;

    private String storageLocation;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime storedAt;

    /** 出庫日時（フロントエンドは retrievedAt で受け取る） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime retrievedAt;

    /** 備考（フロントエンドは remark で受け取る） */
    @JsonProperty("remark")
    private String remarks;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
