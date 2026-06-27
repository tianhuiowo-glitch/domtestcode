package com.dorm.server.constant;

/**
 * 业务提示信息常量类
 * 禁止在业务代码中硬编码提示字符串
 *
 * @author dorm-server
 */
public final class MessageConstants {

    private MessageConstants() {
        // 禁止实例化
    }

    // ==================== 通用 ====================

    /** 操作成功 */
    public static final String SUCCESS = "操作が完了しました";

    /** 数据不存在 */
    public static final String DATA_NOT_FOUND = "データが存在しません";

    /** 版本冲突，请刷新后重试 */
    public static final String VERSION_CONFLICT = "データが他のユーザーによって更新されました。画面を更新してから再試行してください";

    // ==================== 认证相关 ====================

    /** 用户名或密码错误 */
    public static final String LOGIN_FAIL = "ユーザー名またはパスワードが正しくありません";

    /** 账号已禁用 */
    public static final String ACCOUNT_DISABLED = "アカウントが無効化されています。管理者にお問い合わせください";

    /** 登录成功 */
    public static final String LOGIN_SUCCESS = "ログインしました";

    /** 登出成功 */
    public static final String LOGOUT_SUCCESS = "ログアウトしました";

    // ==================== 宿舍相关 ====================

    /** 宿舍不存在 */
    public static final String DORMITORY_NOT_FOUND = "宿舎が見つかりません";

    /** 宿舍名称已存在 */
    public static final String DORMITORY_NAME_EXISTS = "同じ宿舎名が既に存在します";

    /** 宿舍下存在入住者，无法删除 */
    public static final String DORMITORY_HAS_ROOMS = "入居者がいる宿舎は削除できません";

    // ==================== 房间相关 ====================

    /** 房间不存在 */
    public static final String ROOM_NOT_FOUND = "部屋が見つかりません";

    /** 房间已满，无法入住 */
    public static final String ROOM_IS_FULL = "部屋が満室のため入居できません";

    /** 房间下存在入住记录，无法删除 */
    public static final String ROOM_HAS_RESIDENTS = "入居中の方がいる部屋は削除できません";

    // ==================== 入住相关 ====================

    /** 入住记录不存在 */
    public static final String CHECKIN_NOT_FOUND = "入居記録が見つかりません";

    /** 已办理退住，不可重复操作 */
    public static final String ALREADY_CHECKED_OUT = "すでに退居手続き済みの記録です";

    /** 退住日期不能早于入住日期 */
    public static final String CHECKOUT_DATE_INVALID = "退居日は入居日より後に設定してください";

    // ==================== 费用相关 ====================

    /** 费用记录不存在 */
    public static final String FEE_NOT_FOUND = "費用記録が見つかりません";

    /** 费用已确认，不可重复确认 */
    public static final String FEE_ALREADY_CONFIRMED = "費用はすでに確認済みです";

    /** 费用非待确认状态，无法删除 */
    public static final String FEE_NOT_PENDING = "削除できるのは「確認待ち」状態の費用のみです";

    /** 批量删除费用ID列表不能为空 */
    public static final String FEE_DELETE_IDS_EMPTY = "削除する費用IDリストを指定してください";

    // ==================== 设备相关 ====================

    /** 设备不存在 */
    public static final String EQUIPMENT_NOT_FOUND = "備品が見つかりません";

    /** 设备处理记录不存在 */
    public static final String EQUIPMENT_PROCESS_NOT_FOUND = "備品処理記録が見つかりません";

    // ==================== 员工相关 ====================

    /** 社員番号不能为空 */
    public static final String EMPLOYEE_ID_EMPTY = "社員番号は必須です";

    /** 费用ID列表不能为空 */
    public static final String FEE_ID_LIST_EMPTY = "費用IDリストを指定してください";

    // ==================== 导入相关 ====================

    /** 文件格式不支持 */
    public static final String IMPORT_FILE_TYPE_ERROR = ".xlsx または .xls 形式のファイルをアップロードしてください";

    /** 文件为空 */
    public static final String IMPORT_FILE_EMPTY = "アップロードファイルが空です";

    /** 导入任务不存在 */
    public static final String IMPORT_TASK_NOT_FOUND = "インポートタスクが存在しないか、期限切れです";

    // ==================== 性别制约 ====================

    /** 男性寮に女性は入居不可 */
    public static final String GENDER_MISMATCH_MALE_DORM = "男性寮に女性は入居できません";

    /** 女性寮に男性は入居不可 */
    public static final String GENDER_MISMATCH_FEMALE_DORM = "女性寮に男性は入居できません";

    // ==================== 宿舍类型相关 ====================

    /** 宿舎タイプ変更不可（入居者あり） */
    public static final String DORMITORY_TYPE_CHANGE_DENIED = "入居者がいるため、寮タイプを変更できません";
}
