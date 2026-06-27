package com.dorm.server.controller;

import com.alibaba.excel.EasyExcel;
import com.dorm.server.entity.common.Result;
import com.dorm.server.entity.dto.ExecuteImportDTO;
import com.dorm.server.entity.vo.ImportTaskVO;
import com.dorm.server.entity.vo.ImportValidationResultVO;
import com.dorm.server.service.ImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 导入接口 Controller
 *
 * @author dorm-server
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/imports")
@RequiredArgsConstructor
public class ImportController {

    private final ImportService importService;

    /**
     * 上传并校验 Excel 文件
     * POST /api/v1/imports/upload
     *
     * @param file 上传的 Excel 文件（multipart/form-data）
     * @return 校验结果（含tempKey、错误列表、预览数据）
     */
    @PostMapping("/upload")
    public Result<ImportValidationResultVO> upload(@RequestParam("file") MultipartFile file) {
        log.info("[API] POST /api/v1/imports/upload, fileName={}, size={}",
                file.getOriginalFilename(), file.getSize());
        ImportValidationResultVO result = importService.uploadAndValidate(file);
        return Result.success(result);
    }

    /**
     * 执行正式导入（异步）
     * POST /api/v1/imports/execute
     *
     * @param dto 执行参数（含tempKey）
     * @return 任务 VO（taskId 用于轮询状态）
     */
    @PostMapping("/execute")
    public Result<ImportTaskVO> execute(@RequestBody @Validated ExecuteImportDTO dto) {
        log.info("[API] POST /api/v1/imports/execute, tempKey={}", dto.getTempKey());
        ImportTaskVO task = importService.executeImport(dto);
        return Result.success(task);
    }

    /**
     * 查询导入任务状态
     * GET /api/v1/imports/tasks/{taskId}
     *
     * @param taskId 任务ID
     * @return 任务 VO
     */
    @GetMapping("/tasks/{taskId}")
    public Result<ImportTaskVO> getTask(@PathVariable String taskId) {
        log.info("[API] GET /api/v1/imports/tasks/{}", taskId);
        ImportTaskVO task = importService.getTaskStatus(taskId);
        return Result.success(task);
    }

    /**
     * システムフィールド一覧取得
     * GET /api/v1/imports/fields
     *
     * @return フィールド定義リスト（value / label / required）
     */
    @GetMapping("/fields")
    public Result<List<Map<String, Object>>> getFields() {
        List<Map<String, Object>> fields = new ArrayList<>();
        Object[][] defs = {
            {"residentName", "入居者氏名", true},
            {"employeeNo",   "社員番号",   true},
            {"checkInDate",  "入寮日",     true},
            {"checkOutDate", "退寮日",     false},
            {"roomName",     "部屋名",     false},
            {"ignore",       "（無視する）", false},
        };
        for (Object[] def : defs) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("value",    def[0]);
            m.put("label",    def[1]);
            m.put("required", def[2]);
            fields.add(m);
        }
        return Result.success(fields);
    }

    /**
     * インポートテンプレート Excel ダウンロード
     * GET /api/v1/imports/template
     */
    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment; filename*=UTF-8''import_template.xlsx");

        List<List<String>> head = Arrays.asList(
                Arrays.asList("入居者氏名"),
                Arrays.asList("社員番号"),
                Arrays.asList("入寮日(yyyy-MM-dd)"),
                Arrays.asList("退寮日(yyyy-MM-dd)")
        );

        EasyExcel.write(response.getOutputStream())
                .head(head)
                .sheet("テンプレート")
                .doWrite(new ArrayList<>());
    }
}
