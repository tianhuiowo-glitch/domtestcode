package com.dorm.server.service;

import com.dorm.server.entity.vo.EmployeeMasterVO;

import java.util.List;

/**
 * 社員マスタ業務接口
 *
 * @author dorm-server
 */
public interface EmployeeMasterService {

    /**
     * キーワードで社員マスタを検索（コンボボックス用）
     *
     * @param keyword 検索キーワード（氏名・社員番号、null可）
     * @return 社員マスタ VO リスト（最大20件）
     */
    List<EmployeeMasterVO> searchByKeyword(String keyword);

    /**
     * 社員番号で社員マスタを1件取得
     *
     * @param employeeId 社員番号
     * @return 社員マスタ VO
     */
    EmployeeMasterVO getByEmployeeId(String employeeId);

    /**
     * 次の派遣社員番号を生成して返す（プレフィックス対応）
     * residence_histories の既存番号最大値+1 をプレフィックス+6桁ゼロ埋め形式で返す
     * D→大連（D000001+）、S→瀋陽（S000001+）、C→CDXT（C000001+）
     *
     * @param prefix プレフィックス文字（"D"/"S"/"C"など、null または空文字の場合は "D" を使用）
     * @return 次の社員番号（例: D000028、S000003）
     */
    String getNextDispatchId(String prefix);
}
