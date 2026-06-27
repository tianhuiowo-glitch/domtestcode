package com.dorm.server.constant;

/**
 * 系统常量类
 *
 * @author dorm-server
 */
public final class SystemConstants {

    private SystemConstants() {
        // 禁止实例化
    }

    // ==================== 通用状态 ====================

    /** 软删除标志：未删除（deleted_at IS NULL） */
    public static final String NOT_DELETED = "IS NULL";

    // ==================== 入住状态 ====================

    /** 入住状态：在住 */
    public static final String CHECKIN_STATUS_ACTIVE = "active";

    /** 入住状态：已退住 */
    public static final String CHECKIN_STATUS_CHECKED_OUT = "checked_out";

    // ==================== 性别常量 ====================

    /** 性别：男 */
    public static final String GENDER_MALE = "male";

    /** 性别：女 */
    public static final String GENDER_FEMALE = "female";

    // ==================== 宿舍类型 ====================

    /** 宿舍类型：男宿 */
    public static final String DORM_TYPE_MALE = "male";

    /** 宿舍类型：女宿 */
    public static final String DORM_TYPE_FEMALE = "female";

    /** 宿舍类型：混合 */
    public static final String DORM_TYPE_MIXED = "mixed";

    // ==================== 费用状态 ====================

    /** 费用状态：待确认 */
    public static final String FEE_STATUS_PENDING = "pending";

    /** 费用状态：已确认 */
    public static final String FEE_STATUS_CONFIRMED = "confirmed";

    // ==================== 设备状态 ====================

    /** 设备状态：正常 */
    public static final String EQUIPMENT_STATUS_NORMAL = "normal";

    /** 设备状态：损坏 */
    public static final String EQUIPMENT_STATUS_DAMAGED = "damaged";

    /** 设备状态：丢失 */
    public static final String EQUIPMENT_STATUS_LOST = "lost";

    /** 设备状态：库存中 */
    public static final String EQUIPMENT_STATUS_IN_STORAGE = "in_storage";

    // ==================== 设备处理状态 ====================

    /** 处理状态：待处理 */
    public static final String PROCESS_STATUS_PENDING = "pending";

    /** 处理状态：已完成 */
    public static final String PROCESS_STATUS_COMPLETED = "completed";

    // ==================== 预警阈值（天） ====================

    /** 长期入住警告阈值（90天） */
    public static final Integer LONG_TERM_WARNING_DAYS = 90;

    /** 长期入住严重阈值（180天） */
    public static final Integer LONG_TERM_CRITICAL_DAYS = 180;

    /** 退住预警提前天数（15天） */
    public static final Integer WITHDRAWAL_ALERT_DAYS = 15;

    // ==================== 预警级别 ====================

    /** 预警级别：警告 */
    public static final String ALERT_LEVEL_WARNING = "warning";

    /** 预警级别：严重 */
    public static final String ALERT_LEVEL_CRITICAL = "critical";

    // ==================== 导入任务状态 ====================

    /** 导入任务：等待 */
    public static final String IMPORT_STATUS_PENDING = "pending";

    /** 导入任务：处理中 */
    public static final String IMPORT_STATUS_PROCESSING = "processing";

    /** 导入任务：成功（フロントエンドは "completed" を期待） */
    public static final String IMPORT_STATUS_SUCCESS = "completed";

    /** 导入任务：部分成功（フロントエンドは "completed" として扱う） */
    public static final String IMPORT_STATUS_PARTIAL = "completed";

    /** 导入任务：失败 */
    public static final String IMPORT_STATUS_FAILED = "failed";

    // ==================== 操作日志状态 ====================

    /** 操作日志ステータス：成功 */
    public static final String LOG_STATUS_SUCCESS = "success";

    /** 操作日志ステータス：失敗 */
    public static final String LOG_STATUS_FAIL = "fail";

    // ==================== 操作日志动作 ====================

    /** 日志动作：登录 */
    public static final String LOG_ACTION_LOGIN = "LOGIN";

    /** 日志动作：登出 */
    public static final String LOG_ACTION_LOGOUT = "LOGOUT";

    /** 日志动作：新增 */
    public static final String LOG_ACTION_CREATE = "CREATE";

    /** 日志动作：修改 */
    public static final String LOG_ACTION_UPDATE = "UPDATE";

    /** 日志动作：删除 */
    public static final String LOG_ACTION_DELETE = "DELETE";

    // ==================== Redis过期时间（秒） ====================

    /** Token 过期时间：24小时 */
    public static final Long TOKEN_EXPIRE_SECONDS = 86400L;

    /** 导入临时数据过期时间：30分钟 */
    public static final Long IMPORT_TEMP_EXPIRE_SECONDS = 1800L;

    /** 导入任务状态过期时间：1小时 */
    public static final Long IMPORT_TASK_EXPIRE_SECONDS = 3600L;

    /** 字典缓存过期时间：1小时 */
    public static final Long DICT_CACHE_EXPIRE_SECONDS = 3600L;

    /** 汇总统计缓存过期时间：5分钟 */
    public static final Long SUMMARY_CACHE_EXPIRE_SECONDS = 300L;

    // ==================== 分页默认值 ====================

    /** 默认页码 */
    public static final Integer DEFAULT_PAGE = 1;

    /** 默认每页大小 */
    public static final Integer DEFAULT_PAGE_SIZE = 20;
}
