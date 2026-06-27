package com.dorm.server.service;

import com.dorm.server.entity.common.PageVO;
import com.dorm.server.entity.vo.ResidenceChangeLogVO;

/**
 * 入居履歴変更ログ業務接口
 *
 * @author dorm-server
 */
public interface ResidenceChangeLogService {

    /**
     * 分页查询変更ログリスト
     *
     * @param page          页码
     * @param pageSize      每页大小
     * @param operationType 操作タイプ（INSERT/UPDATE/DELETE，null=全部）
     * @param from          開始日時文字列（null=無制限）
     * @param to            終了日時文字列（null=無制限）
     * @return 分页結果
     */
    PageVO<ResidenceChangeLogVO> listChangeLogs(Integer page, Integer pageSize,
                                                String operationType,
                                                String from, String to);

    /**
     * 変更ログを記録する
     *
     * @param residenceHistoryId 入居履歴ID
     * @param operationType      操作タイプ（INSERT/UPDATE/DELETE）
     * @param operatedBy         操作者ユーザー名
     * @param residentName       入居者氏名
     * @param dormitoryName      宿舍名
     * @param roomName           部屋名
     */
    void recordChange(Integer residenceHistoryId, String operationType, String operatedBy,
                      String residentName, String dormitoryName, String roomName);
}
