package com.dorm.server.entity.vo;

import lombok.Data;

import java.util.List;

/**
 * 导入验证结果 VO
 * 文件上传后，返回数据校验结果供用户确认
 *
 * @author dorm-server
 */
@Data
public class ImportValidationResultVO {

    /** 临时存储Key（用于后续执行导入） */
    private String tempKey;

    /** 总行数 */
    private Integer totalRows;

    /** 有效行数 */
    private Integer validRows;

    /** 错误行数 */
    private Integer errorRows;

    /** 错误详情列表 */
    private List<ValidationError> errors;

    /** Excel ヘッダー列名リスト */
    private List<String> columnHeaders;

    /** 预览数据（前10条） */
    private List<Object> previewData;

    /**
     * 单行校验错误信息
     */
    @Data
    public static class ValidationError {
        /** 行号（フロントエンドは row で受け取る） */
        @com.fasterxml.jackson.annotation.JsonProperty("row")
        private Integer rowIndex;
        /** 字段名 */
        private String field;
        /** 错误提示 */
        private String message;
    }
}
