package com.dorm.server.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.data.ReadCellData;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Excel 导入工具类
 * 使用 EasyExcel 解析上传的 Excel 文件
 *
 * @author dorm-server
 */
@Slf4j
@Component
public class ExcelImportUtil {

    /**
     * 读取 Excel 文件，返回原始行数据（Map格式，key为列索引）
     *
     * @param file Excel 文件
     * @return 行数据列表（每行为 Map<Integer, String>）
     */
    public List<Map<Integer, String>> readExcel(MultipartFile file) {
        List<Map<Integer, String>> dataList = new ArrayList<>(64);

        try {
            EasyExcel.read(file.getInputStream())
                    .sheet()
                    .headRowNumber(1)
                    .registerReadListener(new AnalysisEventListener<Map<Integer, String>>() {

                        @Override
                        public void invoke(Map<Integer, String> data, AnalysisContext context) {
                            dataList.add(data);
                        }

                        @Override
                        public void doAfterAllAnalysed(AnalysisContext context) {
                            log.info("[Excel解析] 共解析 {} 行数据", dataList.size());
                        }
                    })
                    .doRead();
        } catch (IOException e) {
            log.error("[Excel解析失败] {}", e.getMessage(), e);
            throw new RuntimeException("Excel 文件解析失败: " + e.getMessage());
        }

        return dataList;
    }

    /**
     * 读取 Excel 文件，指定数据模型类进行解析
     *
     * @param file       Excel 文件
     * @param modelClass 数据模型类（需要 @ExcelProperty 注解）
     * @param <T>        数据类型
     * @return 解析后的数据列表
     */
    public <T> List<T> readExcel(MultipartFile file, Class<T> modelClass) {
        List<T> dataList = new ArrayList<>(64);

        try {
            EasyExcel.read(file.getInputStream(), modelClass,
                    new AnalysisEventListener<T>() {
                        @Override
                        public void invoke(T data, AnalysisContext context) {
                            dataList.add(data);
                        }

                        @Override
                        public void doAfterAllAnalysed(AnalysisContext context) {
                            log.info("[Excel解析] 模型={}, 共解析 {} 行数据",
                                    modelClass.getSimpleName(), dataList.size());
                        }
                    })
                    .sheet()
                    .headRowNumber(1)
                    .doRead();
        } catch (IOException e) {
            log.error("[Excel解析失败] 模型={}, error={}", modelClass.getSimpleName(), e.getMessage(), e);
            throw new RuntimeException("Excel 文件解析失败: " + e.getMessage());
        }

        return dataList;
    }

    /**
     * ヘッダー行とデータ行を同時に取得する
     */
    public ExcelReadResult readExcelWithHeaders(MultipartFile file) {
        List<String> headers = new ArrayList<>();
        List<Map<Integer, String>> rows = new ArrayList<>(64);

        try {
            EasyExcel.read(file.getInputStream())
                    .sheet()
                    .headRowNumber(1)
                    .registerReadListener(new AnalysisEventListener<Map<Integer, String>>() {

                        @Override
                        public void invokeHead(Map<Integer, ReadCellData<?>> headMap, AnalysisContext context) {
                            headMap.entrySet().stream()
                                    .sorted(Map.Entry.comparingByKey())
                                    .forEach(e -> {
                                        String val = e.getValue().getStringValue();
                                        headers.add(val != null ? val : "");
                                    });
                        }

                        @Override
                        public void invoke(Map<Integer, String> data, AnalysisContext context) {
                            rows.add(data);
                        }

                        @Override
                        public void doAfterAllAnalysed(AnalysisContext context) {
                            log.info("[Excel解析] ヘッダー数={}, データ行数={}", headers.size(), rows.size());
                        }
                    })
                    .doRead();
        } catch (IOException e) {
            log.error("[Excel解析失敗] {}", e.getMessage(), e);
            throw new RuntimeException("Excel ファイルの解析に失敗しました: " + e.getMessage());
        }

        ExcelReadResult result = new ExcelReadResult();
        result.setHeaders(headers);
        result.setRows(rows);
        return result;
    }

    @Data
    public static class ExcelReadResult {
        private List<String> headers;
        private List<Map<Integer, String>> rows;
    }

    /**
     * 校验文件格式是否为 Excel
     *
     * @param file 上传文件
     * @return true=合法 Excel 格式
     */
    public boolean isExcelFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return false;
        }
        String lowerName = originalFilename.toLowerCase();
        return lowerName.endsWith(".xlsx") || lowerName.endsWith(".xls");
    }
}
