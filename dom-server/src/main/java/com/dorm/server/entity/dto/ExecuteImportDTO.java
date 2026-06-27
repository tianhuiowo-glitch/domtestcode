package com.dorm.server.entity.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * インポート実行 DTO
 * アップロード検証フェーズで返された tempKey を使って本インポートを実行する
 *
 * @author dorm-server
 */
@Data
public class ExecuteImportDTO {

    /** 一時保存キー（/imports/upload 返却値の tempKey） */
    @NotBlank(message = "tempKey不能为空")
    private String tempKey;

    /** 選択行インデックスリスト（フロントエンドが送信） */
    private List<Integer> selectedRows;

    /** 操作者ユーザー名 */
    private String operatedBy;
}
