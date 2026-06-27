package com.dorm.server.controller;

import com.dorm.server.service.ExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * CSV エクスポート Controller
 *
 * @author dorm-server
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/exports")
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;

    /**
     * CSV エクスポートダウンロード
     * GET /api/v1/exports?type=residence&region_id=1
     * GET /api/v1/exports?type=fees&year=2026&month=6&region_id=1
     *
     * @param type     エクスポート種別（"residence" / "fees"）
     * @param regionId 地域ID（省略 = 全地域）
     * @param year     対象年（fees のみ）
     * @param month    対象月（fees のみ）
     */
    @GetMapping
    public void export(HttpServletResponse response,
                       @RequestParam String type,
                       @RequestParam(name = "region_id", required = false) Integer regionId,
                       @RequestParam(required = false) Integer year,
                       @RequestParam(required = false) Integer month) throws IOException {
        log.info("[API] GET /api/v1/exports type={}, regionId={}, year={}, month={}", type, regionId, year, month);
        exportService.exportCsv(response, type, regionId, year, month);
    }
}
