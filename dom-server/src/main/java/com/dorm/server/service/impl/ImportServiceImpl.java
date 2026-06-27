package com.dorm.server.service.impl;

import com.dorm.server.constant.MessageConstants;
import com.dorm.server.constant.RedisKeyConstants;
import com.dorm.server.constant.SystemConstants;
import com.dorm.server.entity.ResidenceHistory;
import com.dorm.server.entity.dto.ExecuteImportDTO;
import com.dorm.server.entity.vo.ImportTaskVO;
import com.dorm.server.entity.vo.ImportValidationResultVO;
import com.dorm.server.exception.BusinessException;
import com.dorm.server.mapper.ResidenceHistoryMapper;
import com.dorm.server.service.ImportService;
import com.dorm.server.util.ExcelImportUtil;
import com.dorm.server.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 导入业务实现类
 *
 * @author dorm-server
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImportServiceImpl implements ImportService {

    private final ExcelImportUtil excelImportUtil;
    private final RedisUtil redisUtil;
    private final ResidenceHistoryMapper residenceHistoryMapper;

    @Override
    public ImportValidationResultVO uploadAndValidate(MultipartFile file) {
        log.info("[Excel上传] 文件名={}, 大小={}bytes", file.getOriginalFilename(), file.getSize());

        // 校验文件格式
        if (file.isEmpty()) {
            throw new BusinessException(MessageConstants.IMPORT_FILE_EMPTY);
        }
        if (!excelImportUtil.isExcelFile(file)) {
            throw new BusinessException(MessageConstants.IMPORT_FILE_TYPE_ERROR);
        }

        // ヘッダー行とデータ行を同時に取得
        ExcelImportUtil.ExcelReadResult excelResult = excelImportUtil.readExcelWithHeaders(file);
        List<Map<Integer, String>> rawData = excelResult.getRows();

        // 构建校验结果
        ImportValidationResultVO result = new ImportValidationResultVO();
        result.setColumnHeaders(excelResult.getHeaders());
        List<ImportValidationResultVO.ValidationError> errors = new ArrayList<>(16);
        List<Object> validRows = new ArrayList<>(rawData.size());

        int rowIndex = 1;
        for (Map<Integer, String> row : rawData) {
            // 校验必填字段（社員名不能为空）
            String residentName = row.get(0);
            if (residentName == null || residentName.trim().isEmpty()) {
                ImportValidationResultVO.ValidationError error = new ImportValidationResultVO.ValidationError();
                error.setRowIndex(rowIndex);
                error.setField("residentName");
                error.setMessage("社員名不能为空");
                errors.add(error);
            } else {
                validRows.add(row);
            }
            rowIndex++;
        }

        // 生成 tempKey，将有效数据暂存 Redis（30分钟）
        String tempKey = UUID.randomUUID().toString().replace("-", "");
        redisUtil.set(RedisKeyConstants.IMPORT_TEMP + tempKey, validRows,
                SystemConstants.IMPORT_TEMP_EXPIRE_SECONDS, TimeUnit.SECONDS);

        result.setTempKey(tempKey);
        result.setTotalRows(rawData.size());
        result.setValidRows(validRows.size());
        result.setErrorRows(errors.size());
        result.setErrors(errors);
        // 返回前10条预览
        result.setPreviewData(validRows.subList(0, Math.min(10, validRows.size())));

        log.info("[Excel上传] 总行={}, 有效={}, 错误={}", rawData.size(), validRows.size(), errors.size());
        return result;
    }

    @Override
    public ImportTaskVO executeImport(ExecuteImportDTO dto) {
        // 生成任务ID
        String taskId = UUID.randomUUID().toString().replace("-", "");

        // 初始化任务状态
        ImportTaskVO task = new ImportTaskVO();
        task.setTaskId(taskId);
        task.setStatus(SystemConstants.IMPORT_STATUS_PENDING);
        task.setStartedAt(LocalDateTime.now());

        // 将任务状态写入 Redis
        redisUtil.set(RedisKeyConstants.IMPORT_TASK + taskId, task,
                SystemConstants.IMPORT_TASK_EXPIRE_SECONDS, TimeUnit.SECONDS);

        // 异步执行导入
        doImportAsync(taskId, dto.getTempKey());

        log.info("[导入任务] taskId={} 已提交", taskId);
        return task;
    }

    /**
     * 异步执行实际导入逻辑
     */
    @Async
    public void doImportAsync(String taskId, String tempKey) {
        // 更新任务状态为处理中
        ImportTaskVO task = getTaskFromRedis(taskId);
        if (task == null) {
            return;
        }
        task.setStatus(SystemConstants.IMPORT_STATUS_PROCESSING);
        saveTaskToRedis(taskId, task);

        try {
            // 从 Redis 取出临时数据
            Object cachedData = redisUtil.get(RedisKeyConstants.IMPORT_TEMP + tempKey);
            if (cachedData == null) {
                task.setStatus(SystemConstants.IMPORT_STATUS_FAILED);
                task.setErrorSummary(MessageConstants.IMPORT_TASK_NOT_FOUND);
                saveTaskToRedis(taskId, task);
                return;
            }

            @SuppressWarnings("unchecked")
            List<Object> dataList = (List<Object>) cachedData;
            int totalRows = dataList.size();

            // 在独立事务中执行批量写入，保证原子性
            int[] result = batchInsertResidences(dataList);
            int successRows = result[0];
            int failedRows = result[1];

            // 清理临时数据
            redisUtil.delete(RedisKeyConstants.IMPORT_TEMP + tempKey);

            // 更新任务完成状态
            task.setTotalRows(totalRows);
            task.setSuccessRows(successRows);
            task.setFailedRows(failedRows);
            task.setFinishedAt(LocalDateTime.now());
            task.setStatus(failedRows == 0
                    ? SystemConstants.IMPORT_STATUS_SUCCESS
                    : SystemConstants.IMPORT_STATUS_PARTIAL);
            saveTaskToRedis(taskId, task);

            log.info("[导入完成] taskId={}, 成功={}, 失败={}", taskId, successRows, failedRows);

        } catch (Exception e) {
            log.error("[导入异常] taskId={}, error={}", taskId, e.getMessage(), e);
            task.setStatus(SystemConstants.IMPORT_STATUS_FAILED);
            task.setErrorSummary(e.getMessage());
            task.setFinishedAt(LocalDateTime.now());
            saveTaskToRedis(taskId, task);
        }
    }

    @Override
    public ImportTaskVO getTaskStatus(String taskId) {
        ImportTaskVO task = getTaskFromRedis(taskId);
        if (task == null) {
            throw new BusinessException(MessageConstants.IMPORT_TASK_NOT_FOUND);
        }
        return task;
    }

    /**
     * 批量写入入居记录（含事务控制，失败时整体回滚）
     *
     * @param dataList 有效行数据列表（每行为 Map&lt;Integer, String&gt; 列索引→单元格值）
     * @return int[]{successRows, failedRows}
     */
    @Transactional(rollbackFor = Exception.class)
    public int[] batchInsertResidences(List<Object> dataList) {
        int successRows = 0;
        int failedRows = 0;
        for (Object row : dataList) {
            try {
                @SuppressWarnings("unchecked")
                Map<Integer, String> cells = (Map<Integer, String>) row;

                ResidenceHistory entity = new ResidenceHistory();
                // 第0列：社員名
                entity.setResidentName(getCell(cells, 0));
                // 第1列：社員番号
                entity.setEmployeeId(getCell(cells, 1));
                // 第2列：入住日期（yyyy-MM-dd）
                String checkInDateStr = getCell(cells, 2);
                if (checkInDateStr != null && !checkInDateStr.isBlank()) {
                    entity.setCheckInDate(java.time.LocalDate.parse(checkInDateStr.trim()));
                } else {
                    entity.setCheckInDate(java.time.LocalDate.now());
                }
                // 第3列：计划退住日期（可选）
                String plannedCheckoutStr = getCell(cells, 3);
                if (plannedCheckoutStr != null && !plannedCheckoutStr.isBlank()) {
                    entity.setPlannedCheckoutDate(java.time.LocalDate.parse(plannedCheckoutStr.trim()));
                }
                entity.setVersion(1);
                entity.setIsResponsible(false);

                residenceHistoryMapper.insert(entity);
                successRows++;
            } catch (Exception e) {
                log.warn("[导入] 行导入失败: {}", e.getMessage());
                failedRows++;
            }
        }
        return new int[]{successRows, failedRows};
    }

    /**
     * 安全取单元格值（空白字符串转 null）
     *
     * @param cells 列索引→单元格值映射
     * @param col   列索引（0起始）
     * @return 去除首尾空白后的值，或 null
     */
    private String getCell(Map<Integer, String> cells, int col) {
        String val = cells.get(col);
        return (val != null && !val.isBlank()) ? val.trim() : null;
    }

    /**
     * 从 Redis 获取任务对象
     */
    private ImportTaskVO getTaskFromRedis(String taskId) {
        Object cached = redisUtil.get(RedisKeyConstants.IMPORT_TASK + taskId);
        if (cached instanceof ImportTaskVO) {
            return (ImportTaskVO) cached;
        }
        return null;
    }

    /**
     * 将任务状态保存到 Redis
     */
    private void saveTaskToRedis(String taskId, ImportTaskVO task) {
        redisUtil.set(RedisKeyConstants.IMPORT_TASK + taskId, task,
                SystemConstants.IMPORT_TASK_EXPIRE_SECONDS, TimeUnit.SECONDS);
    }
}
