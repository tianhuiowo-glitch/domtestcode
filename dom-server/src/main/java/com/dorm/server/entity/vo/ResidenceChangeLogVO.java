package com.dorm.server.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 入居履歴変更ログ VO（フロントエンド返却用）
 *
 * @author dorm-server
 */
@Data
public class ResidenceChangeLogVO {

    /** 主键ID */
    private Long id;

    /** 入居履歴ID */
    private Integer residenceHistoryId;

    /** 操作タイプ（INSERT/UPDATE/DELETE） */
    private String operationType;

    /** 操作者ユーザー名 */
    private String operatedBy;

    /** 操作時刻 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operatedAt;

    /** 入居者氏名 */
    private String residentName;

    /** 宿舍名 */
    private String dormitoryName;

    /** 部屋名 */
    private String roomName;
}
