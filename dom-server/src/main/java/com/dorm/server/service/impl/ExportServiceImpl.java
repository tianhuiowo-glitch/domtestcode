package com.dorm.server.service.impl;

import com.dorm.server.mapper.ExportMapper;
import com.dorm.server.service.ExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * CSV エクスポート業務実装クラス
 *
 * @author dorm-server
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExportServiceImpl implements ExportService {

    private static final String BOM = "﻿";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final ExportMapper exportMapper;

    @Override
    public void exportCsv(HttpServletResponse response,
                           String type,
                           Integer regionId,
                           Integer year,
                           Integer month) throws IOException {

        List<LinkedHashMap<String, Object>> data;
        String filename;

        if ("fees".equalsIgnoreCase(type)) {
            data = exportMapper.selectFeeExportData(year, month, regionId);
            filename = "fees_export_" + LocalDate.now().format(DATE_FMT) + ".csv";
        } else {
            data = exportMapper.selectResidenceExportData(regionId);
            filename = "residence_export_" + LocalDate.now().format(DATE_FMT) + ".csv";
        }

        log.info("[エクスポート] type={}, regionId={}, year={}, month={}, 件数={}",
                type, regionId, year, month, data.size());

        response.setContentType("application/octet-stream; charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename*=UTF-8''" + filename);

        try (PrintWriter writer = response.getWriter()) {
            writer.print(BOM);

            if (!data.isEmpty()) {
                // ヘッダー行出力
                writer.println(toCsvLine(data.get(0).keySet().toArray(new String[0])));
                // データ行出力
                for (LinkedHashMap<String, Object> row : data) {
                    writer.println(toCsvLine(row.values().stream()
                            .map(v -> v == null ? "" : String.valueOf(v))
                            .toArray(String[]::new)));
                }
            }
            writer.flush();
        }
    }

    private String toCsvLine(String[] values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(escapeCsv(values[i]));
        }
        return sb.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
