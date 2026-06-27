package com.dorm.server.service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * CSV エクスポート業務インターフェース
 *
 * @author dorm-server
 */
public interface ExportService {

    /**
     * CSV エクスポートを実行し、レスポンスに直接書き出す
     *
     * @param response HttpServletResponse
     * @param type     エクスポート種別（"residence" / "fees"）
     * @param regionId 地域ID（null = 全地域）
     * @param year     対象年（fees のみ使用、null = 全年）
     * @param month    対象月（fees のみ使用、null = 全月）
     */
    void exportCsv(HttpServletResponse response,
                   String type,
                   Integer regionId,
                   Integer year,
                   Integer month) throws IOException;
}
