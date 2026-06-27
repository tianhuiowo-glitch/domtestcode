package com.dorm.server.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * インポートタスク VO
 *
 * @author dorm-server
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImportTaskVO {

    private String taskId;

    /**
     * タスクステータス：
     * pending / processing / completed / failed
     */
    private String status;

    private Integer totalRows;

    private Integer successRows;

    private Integer failedRows;

    /** フロントエンドが参照する処理済み行数（successRows + failedRows） */
    public Integer getProcessedRows() {
        int s = successRows != null ? successRows : 0;
        int f = failedRows != null ? failedRows : 0;
        return s + f;
    }

    /** エラーサマリ（フロントエンドは message で受け取る） */
    @JsonProperty("message")
    private String errorSummary;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime finishedAt;
}
