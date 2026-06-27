package com.dorm.server.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 入居履歴変更ログ実体類
 * 映射 residence_change_logs 表
 *
 * @author dorm-server
 */
@Data
public class ResidenceChangeLog {

    /** 主键ID */
    private Long id;

    /** 入居履歴ID（residence_histories.id） */
    private Integer residenceHistoryId;

    /** 操作タイプ（INSERT/UPDATE/DELETE） */
    private String operationType;

    /** 操作者ユーザー名 */
    private String operatedBy;

    /** 操作時刻 */
    private LocalDateTime operatedAt;

    /** 入居者氏名 */
    private String residentName;

    /** 宿舍名 */
    private String dormitoryName;

    /** 部屋名 */
    private String roomName;
}
