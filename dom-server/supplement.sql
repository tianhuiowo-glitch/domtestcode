-- ============================================================
-- 補足テーブル作成スクリプト & ALTER TABLE 変更スクリプト
-- モジュール：社員寮管理システム - 補足テーブル構造
-- 作成日：2026-06-06
-- ============================================================

SET NAMES utf8mb4;

-- ============================================================
-- 一、既存テーブルへのカラム追加
-- ============================================================

-- 1. residence_histories カラム追加
ALTER TABLE `residence_histories`
    ADD COLUMN `employee_id` VARCHAR(50) NULL COMMENT '社員番号' AFTER `id`,
    ADD COLUMN `gender` VARCHAR(10) NULL COMMENT '性別(male/female)' AFTER `employee_id`,
    ADD COLUMN `planned_checkout_date` DATE NULL COMMENT '退寮予定日' AFTER `check_out_date`;

-- 2. dormitories カラム追加
ALTER TABLE `dormitories`
    ADD COLUMN `dormitory_type` VARCHAR(20) NULL DEFAULT 'mixed' COMMENT '寮タイプ(male/female/mixed)' AFTER `name`;

-- 3. equipment カラム追加
ALTER TABLE `equipment`
    ADD COLUMN `name` VARCHAR(100) NULL COMMENT '設備名称' AFTER `id`,
    ADD COLUMN `category` VARCHAR(50) NULL COMMENT '設備カテゴリ' AFTER `name`,
    ADD COLUMN `serial_number` VARCHAR(100) NULL COMMENT '管理番号' AFTER `category`,
    ADD COLUMN `status` VARCHAR(20) NOT NULL DEFAULT 'normal' COMMENT 'ステータス(normal/damaged/lost/in_storage)' AFTER `serial_number`,
    ADD COLUMN `dormitory_id` INT NULL COMMENT '寮ID' AFTER `status`,
    ADD COLUMN `checkin_id` INT NULL COMMENT '入居記録ID' AFTER `dormitory_id`;

-- ============================================================
-- 二、新規テーブル作成
-- ============================================================

-- 1. システムユーザーテーブル
CREATE TABLE IF NOT EXISTS `sys_users` (
    `id`            BIGINT NOT NULL AUTO_INCREMENT COMMENT '主キーID',
    `username`      VARCHAR(50) NOT NULL COMMENT 'ユーザー名',
    `password`      VARCHAR(255) NOT NULL COMMENT 'パスワード（BCrypt暗号化）',
    `real_name`     VARCHAR(100) NULL COMMENT '氏名',
    `role`          VARCHAR(30) NOT NULL DEFAULT 'operator' COMMENT 'ロール(admin/operator)',
    `status`        TINYINT NOT NULL DEFAULT 1 COMMENT 'ステータス(0:無効 1:有効)',
    `last_login_at` DATETIME NULL COMMENT '最終ログイン日時',
    `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
    `updated_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日時',
    `deleted_at`    DATETIME NULL DEFAULT NULL COMMENT '削除日時',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='システムユーザーテーブル';

-- 管理者アカウント初期化（パスワード：Admin@123 の BCrypt ハッシュ）
INSERT INTO `sys_users` (`username`, `password`, `real_name`, `role`, `status`)
VALUES ('admin', '$2b$10$BBjpzAz6eCCaTIsZD6.yDuV33tKakMzCGxY9JLbs27fGcvaxyTLZS', 'システム管理者', 'admin', 1);

-- 2. 寮費テーブル
CREATE TABLE IF NOT EXISTS `fees` (
    `id`                  BIGINT NOT NULL AUTO_INCREMENT COMMENT '主キーID',
    `residence_id`        INT NOT NULL COMMENT '入居履歴ID',
    `employee_id`         VARCHAR(50) NULL COMMENT '社員番号',
    `employee_name`       VARCHAR(100) NULL COMMENT '社員名',
    `dormitory_id`        INT NULL COMMENT '寮ID',
    `dormitory_name`      VARCHAR(100) NULL COMMENT '寮名',
    `room_id`             INT NULL COMMENT '部屋ID',
    `room_name`           VARCHAR(50) NULL COMMENT '部屋名',
    `period_start`        DATE NOT NULL COMMENT '費用期間開始日',
    `period_end`          DATE NOT NULL COMMENT '費用期間終了日',
    `stay_days`           INT NOT NULL COMMENT '滞在日数',
    `daily_rate`          DECIMAL(10,2) NOT NULL COMMENT '日額',
    `base_amount`         DECIMAL(10,2) NOT NULL COMMENT '基本費用',
    `daily_supplies_cost` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '日用品費',
    `total_amount`        DECIMAL(10,2) NOT NULL COMMENT '合計金額',
    `status`              VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状態(pending/confirmed)',
    `confirmed_at`        DATETIME NULL COMMENT '確定日時',
    `created_at`          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
    `updated_at`          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日時',
    `deleted_at`          DATETIME NULL DEFAULT NULL COMMENT '削除日時',
    PRIMARY KEY (`id`),
    KEY `idx_residence_id` (`residence_id`),
    KEY `idx_employee_id` (`employee_id`),
    KEY `idx_dormitory_id` (`dormitory_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='寮費テーブル';

-- 3. システム操作ログテーブル
CREATE TABLE IF NOT EXISTS `operation_logs` (
    `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT '主キーID',
    `username`    VARCHAR(100) NOT NULL COMMENT '操作ユーザー名',
    `action`      VARCHAR(100) NOT NULL COMMENT '操作アクション',
    `resource`    VARCHAR(100) NULL COMMENT '操作対象リソース',
    `resource_id` VARCHAR(50) NULL COMMENT 'リソースID',
    `detail`      TEXT NULL COMMENT '操作詳細（JSON）',
    `ip_address`  VARCHAR(50) NULL COMMENT '操作元IPアドレス',
    `status`      VARCHAR(20) NOT NULL DEFAULT 'success' COMMENT '操作結果(success/fail)',
    `operated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作日時',
    PRIMARY KEY (`id`),
    KEY `idx_username` (`username`),
    KEY `idx_action` (`action`),
    KEY `idx_resource` (`resource`),
    KEY `idx_operated_at` (`operated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='システム操作ログ';

-- 4. 設備処理記録テーブル（破損・紛失）
CREATE TABLE IF NOT EXISTS `equipment_processes` (
    `id`            BIGINT NOT NULL AUTO_INCREMENT COMMENT '主キーID',
    `equipment_id`  INT NOT NULL COMMENT '設備ID',
    `checkin_id`    INT NULL COMMENT '入居履歴ID',
    `process_type`  VARCHAR(20) NOT NULL COMMENT '処理タイプ(damaged/lost)',
    `description`   TEXT NULL COMMENT '処理内容',
    `cost`          DECIMAL(10,2) NULL COMMENT '処理費用',
    `status`        VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT 'ステータス(pending/completed)',
    `completed_at`  DATETIME NULL COMMENT '完了日時',
    `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
    `updated_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日時',
    `deleted_at`    DATETIME NULL DEFAULT NULL COMMENT '削除日時',
    PRIMARY KEY (`id`),
    KEY `idx_equipment_id` (`equipment_id`),
    KEY `idx_checkin_id` (`checkin_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='設備処理記録';

-- 5. 設備在庫テーブル
CREATE TABLE IF NOT EXISTS `equipment_storage` (
    `id`               BIGINT NOT NULL AUTO_INCREMENT COMMENT '主キーID',
    `equipment_id`     INT NOT NULL COMMENT '設備ID',
    `equipment_name`   VARCHAR(100) NULL COMMENT '設備名',
    `category`         VARCHAR(50) NULL COMMENT '設備分類',
    `serial_number`    VARCHAR(100) NULL COMMENT '管理番号',
    `storage_location` VARCHAR(100) NULL COMMENT '保管場所',
    `stored_at`        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '入庫日時',
    `remarks`          TEXT NULL COMMENT '備考',
    `created_at`       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
    `updated_at`       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日時',
    `deleted_at`       DATETIME NULL DEFAULT NULL COMMENT '削除日時',
    PRIMARY KEY (`id`),
    KEY `idx_equipment_id` (`equipment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='設備在庫テーブル';

-- ============================================================
-- 三、外部キー制約補充
-- ============================================================

ALTER TABLE `fees`
    ADD CONSTRAINT `fk_fees_residence`  FOREIGN KEY (`residence_id`) REFERENCES `residence_histories` (`id`),
    ADD CONSTRAINT `fk_fees_dormitory`  FOREIGN KEY (`dormitory_id`) REFERENCES `dormitories` (`id`),
    ADD CONSTRAINT `fk_fees_room`       FOREIGN KEY (`room_id`)      REFERENCES `rooms` (`id`);

ALTER TABLE `equipment_processes`
    ADD CONSTRAINT `fk_ep_equipment` FOREIGN KEY (`equipment_id`) REFERENCES `equipment` (`id`);

ALTER TABLE `equipment_storage`
    ADD CONSTRAINT `fk_es_equipment` FOREIGN KEY (`equipment_id`) REFERENCES `equipment` (`id`);

-- ============================================================
-- 四、インデックス補充（equipment 新規カラム用）
-- ============================================================

ALTER TABLE `equipment`
    ADD INDEX `idx_equipment_dormitory_id` (`dormitory_id`),
    ADD INDEX `idx_equipment_checkin_id`   (`checkin_id`);

-- ============================================================
-- 五、孤立テーブルの削除（対応Entity/Mapper無し、他テーブルで機能をカバー済み）
-- ============================================================

-- residence_change_logs：変更履歴機能は operation_logs テーブルに統合済み
DROP TABLE IF EXISTS `residence_change_logs`;

-- daily_supplies_costs：日用品費は FeeServiceImpl にて 0 固定、コード参照無し
DROP TABLE IF EXISTS `daily_supplies_costs`;
