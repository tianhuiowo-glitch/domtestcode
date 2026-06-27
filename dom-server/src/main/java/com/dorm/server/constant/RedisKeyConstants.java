package com.dorm.server.constant;

/**
 * Redis Key 常量类
 * 命名规范：dorm:模块名:业务名
 *
 * @author dorm-server
 */
public final class RedisKeyConstants {

    private RedisKeyConstants() {
        // 禁止实例化
    }

    /** 认证模块：Token缓存，格式：dorm:auth:token:{userId} */
    public static final String AUTH_TOKEN = "dorm:auth:token:";

    /** 导入模块：临时数据缓存，格式：dorm:import:temp:{tempKey} */
    public static final String IMPORT_TEMP = "dorm:import:temp:";

    /** 导入模块：任务状态缓存，格式：dorm:import:task:{taskId} */
    public static final String IMPORT_TASK = "dorm:import:task:";

    /** 地域列表缓存 */
    public static final String REGION_LIST = "dorm:region:list";

    /** 部门列表缓存 */
    public static final String DEPARTMENT_LIST = "dorm:department:list";

    /** 空房汇总缓存（5分钟） */
    public static final String VACANCY_SUMMARY = "dorm:vacancy:summary";

    /** 预警汇总缓存（10分钟） */
    public static final String ALERT_SUMMARY = "dorm:alert:summary";
}
