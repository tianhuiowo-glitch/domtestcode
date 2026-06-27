package com.dorm.server.entity.vo;

import lombok.Data;

/**
 * 寮費月次一括生成 結果 VO
 *
 * @author dorm-server
 */
@Data
public class GenerateFeeResultVO {

    /**
     * 新規生成件数
     */
    private Integer generated;

    /**
     * 重複スキップ件数（該当期間の費用がすでに存在）
     */
    private Integer skipped;

    /**
     * 対象入居件数（generated + skipped の合計）
     */
    private Integer total;
}
