package com.dorm.server.service;

import com.dorm.server.entity.dto.ExecuteImportDTO;
import com.dorm.server.entity.vo.ImportTaskVO;
import com.dorm.server.entity.vo.ImportValidationResultVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 导入业务接口
 *
 * @author dorm-server
 */
public interface ImportService {

    /**
     * 上传并校验 Excel 文件
     * 解析文件、校验数据格式，将有效数据暂存 Redis
     *
     * @param file 上传的 Excel 文件
     * @return 校验结果（含 tempKey 供后续执行导入使用）
     */
    ImportValidationResultVO uploadAndValidate(MultipartFile file);

    /**
     * 执行正式导入（异步）
     * 从 Redis 取出临时数据，执行数据库批量写入
     *
     * @param dto 执行参数（含tempKey）
     * @return 任务 VO（taskId 用于轮询状态）
     */
    ImportTaskVO executeImport(ExecuteImportDTO dto);

    /**
     * 查询导入任务状态
     *
     * @param taskId 任务ID
     * @return 任务 VO
     */
    ImportTaskVO getTaskStatus(String taskId);
}
