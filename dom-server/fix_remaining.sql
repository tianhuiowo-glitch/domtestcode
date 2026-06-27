-- ============================================================
-- 修正スクリプト：supplement.sql 実行失敗・未実行部分の補完
-- 前提：ALTER TABLE（一章）は成功済み、sys_users テーブルは作成済みだが INSERT 失敗
-- ============================================================

SET NAMES utf8mb4;

-- 1. 管理者アカウントの挿入（パスワード：Admin@123 の BCrypt ハッシュ）
INSERT IGNORE INTO `sys_users` (`username`, `password`, `real_name`, `role`, `status`)
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

-- 4. 設備処理記録テーブル
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

-- 6. 外部キー制約補充
ALTER TABLE `fees`
    ADD CONSTRAINT `fk_fees_residence`  FOREIGN KEY (`residence_id`) REFERENCES `residence_histories` (`id`),
    ADD CONSTRAINT `fk_fees_dormitory`  FOREIGN KEY (`dormitory_id`) REFERENCES `dormitories` (`id`),
    ADD CONSTRAINT `fk_fees_room`       FOREIGN KEY (`room_id`)      REFERENCES `rooms` (`id`);

ALTER TABLE `equipment_processes`
    ADD CONSTRAINT `fk_ep_equipment` FOREIGN KEY (`equipment_id`) REFERENCES `equipment` (`id`);

ALTER TABLE `equipment_storage`
    ADD CONSTRAINT `fk_es_equipment` FOREIGN KEY (`equipment_id`) REFERENCES `equipment` (`id`);

-- 7. インデックス補充（equipment 新規カラム用）
ALTER TABLE `equipment`
    ADD INDEX `idx_equipment_dormitory_id` (`dormitory_id`),
    ADD INDEX `idx_equipment_checkin_id`   (`checkin_id`);

-- 8. 孤立テーブルの削除
DROP TABLE IF EXISTS `residence_change_logs`;
DROP TABLE IF EXISTS `daily_supplies_costs`;
