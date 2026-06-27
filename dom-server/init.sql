-- ============================================================
-- 社員寮管理システム — データベース初期化 SQL
-- 詳細設計書.md 準拠 / Navicat で直接実行可能
-- 生成日: 2026-06-06
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. regions（地域マスタ）
-- ============================================================
CREATE TABLE IF NOT EXISTS `regions` (
  `id`         INT          NOT NULL AUTO_INCREMENT,
  `name`       VARCHAR(50)  NOT NULL COMMENT '地域名',
  `sort_order` INT          NOT NULL COMMENT '表示順',
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` DATETIME     NULL     DEFAULT NULL COMMENT '論理削除日時',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地域マスタ';

INSERT INTO `regions` (`id`, `name`, `sort_order`, `created_at`, `updated_at`) VALUES
(1, '東京',   1, '2026-04-01 09:00:00', '2026-04-01 09:00:00'),
(2, '大阪',   2, '2026-04-01 09:00:00', '2026-04-01 09:00:00'),
(3, '名古屋', 3, '2026-04-01 09:00:00', '2026-04-01 09:00:00'),
(4, 'その他', 4, '2026-04-01 09:00:00', '2026-04-01 09:00:00'),
(5, '福岡',   5, '2026-04-01 09:00:00', '2026-04-01 09:00:00');

-- ============================================================
-- 2. departments（所属マスタ）
-- ============================================================
CREATE TABLE IF NOT EXISTS `departments` (
  `id`         INT          NOT NULL AUTO_INCREMENT,
  `name`       VARCHAR(100) NOT NULL COMMENT '所属名',
  `sort_order` INT          NOT NULL COMMENT '表示順',
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` DATETIME     NULL     DEFAULT NULL COMMENT '論理削除日時',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='所属マスタ';

INSERT INTO `departments` (`id`, `name`, `sort_order`, `created_at`, `updated_at`) VALUES
(1, '営業部',     1, '2026-04-01 09:00:00', '2026-04-01 09:00:00'),
(2, '総務部',     2, '2026-04-01 09:00:00', '2026-04-01 09:00:00'),
(3, '開発部',     3, '2026-04-01 09:00:00', '2026-04-01 09:00:00'),
(4, '製造部',     4, '2026-04-01 09:00:00', '2026-04-01 09:00:00'),
(5, '人事部',     5, '2026-04-01 09:00:00', '2026-04-01 09:00:00');

-- ============================================================
-- 3. dormitories（寮マスタ）
-- ============================================================
CREATE TABLE IF NOT EXISTS `dormitories` (
  `id`         INT            NOT NULL AUTO_INCREMENT,
  `region_id`  INT            NOT NULL COMMENT '地域ID',
  `name`       VARCHAR(100)   NOT NULL COMMENT '寮名',
  `address`    VARCHAR(255)   NULL     DEFAULT NULL COMMENT '住所',
  `daily_rate` DECIMAL(10,2)  NOT NULL COMMENT '日額単価（円）',
  `sort_order` INT            NOT NULL COMMENT '表示順',
  `version`    INT            NOT NULL DEFAULT 1 COMMENT '楽観ロック',
  `created_at` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` DATETIME       NULL     DEFAULT NULL COMMENT '論理削除',
  PRIMARY KEY (`id`),
  INDEX `idx_dormitories_region_id` (`region_id`),
  CONSTRAINT `fk_dormitories_region` FOREIGN KEY (`region_id`) REFERENCES `regions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='寮マスタ';

INSERT INTO `dormitories` (`id`, `region_id`, `name`, `address`, `daily_rate`, `sort_order`, `version`, `created_at`, `updated_at`) VALUES
(1, 1, '第一寮',   '東京都新宿区西新宿1-1-1',   1000.00, 1, 1, '2026-04-01 09:00:00', '2026-04-01 09:00:00'),
(2, 1, '第二寮',   '東京都渋谷区渋谷2-2-2',      900.00,  2, 1, '2026-04-01 09:00:00', '2026-04-01 09:00:00'),
(3, 2, '大阪寮',   '大阪府大阪市北区梅田3-3-3',  850.00,  3, 1, '2026-04-01 09:00:00', '2026-04-01 09:00:00'),
(4, 3, '名古屋寮', '愛知県名古屋市中村区4-4-4',   800.00,  4, 1, '2026-04-01 09:00:00', '2026-04-01 09:00:00'),
(5, 4, 'その他寮', '神奈川県横浜市中区5-5-5',     750.00,  5, 1, '2026-04-01 09:00:00', '2026-04-01 09:00:00');

-- ============================================================
-- 4. rooms（部屋マスタ）
-- ============================================================
CREATE TABLE IF NOT EXISTS `rooms` (
  `id`           INT         NOT NULL AUTO_INCREMENT,
  `dormitory_id` INT         NOT NULL COMMENT '所属寮ID',
  `name`         VARCHAR(50) NOT NULL COMMENT '部屋名（例：101号室）',
  `capacity`     INT         NOT NULL DEFAULT 1 COMMENT '定員（原則1名）',
  `version`      INT         NOT NULL DEFAULT 1 COMMENT '楽観ロック',
  `created_at`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at`   DATETIME    NULL     DEFAULT NULL COMMENT '論理削除',
  PRIMARY KEY (`id`),
  INDEX `idx_rooms_dormitory_id` (`dormitory_id`),
  CONSTRAINT `fk_rooms_dormitory` FOREIGN KEY (`dormitory_id`) REFERENCES `dormitories` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部屋マスタ';

INSERT INTO `rooms` (`id`, `dormitory_id`, `name`, `capacity`, `version`, `created_at`, `updated_at`) VALUES
(1, 1, '101号室', 1, 1, '2026-04-01 09:00:00', '2026-04-01 09:00:00'),
(2, 1, '102号室', 1, 1, '2026-04-01 09:00:00', '2026-04-01 09:00:00'),
(3, 2, '201号室', 1, 1, '2026-04-01 09:00:00', '2026-04-01 09:00:00'),
(4, 3, '301号室', 1, 1, '2026-04-01 09:00:00', '2026-04-01 09:00:00'),
(5, 4, '401号室', 1, 1, '2026-04-01 09:00:00', '2026-04-01 09:00:00');

-- ============================================================
-- 5. residence_histories（入居履歴）※業務中核テーブル
-- ============================================================
CREATE TABLE IF NOT EXISTS `residence_histories` (
  `id`              INT          NOT NULL AUTO_INCREMENT,
  `room_id`         INT          NOT NULL COMMENT '入居部屋ID',
  `department_id`   INT          NOT NULL COMMENT '所属ID',
  `resident_name`   VARCHAR(100) NOT NULL COMMENT '入居者氏名',
  `check_in_date`   DATE         NOT NULL COMMENT '入寮日',
  `check_out_date`  DATE         NULL     DEFAULT NULL COMMENT '退寮日（NULL=無期限）',
  `is_responsible`  TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '責任者フラグ（1=★）',
  `remarks`         TEXT         NULL     DEFAULT NULL COMMENT '備考',
  `version`         INT          NOT NULL DEFAULT 1 COMMENT '楽観ロック',
  `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at`      DATETIME     NULL     DEFAULT NULL COMMENT '論理削除',
  PRIMARY KEY (`id`),
  INDEX `idx_residence_room_id`       (`room_id`),
  INDEX `idx_residence_check_in`      (`check_in_date`),
  INDEX `idx_residence_check_out`     (`check_out_date`),
  INDEX `idx_residence_resident_name` (`resident_name`),
  CONSTRAINT `fk_residence_room`       FOREIGN KEY (`room_id`)       REFERENCES `rooms`       (`id`),
  CONSTRAINT `fk_residence_department` FOREIGN KEY (`department_id`) REFERENCES `departments` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入居履歴（業務中核テーブル）';

INSERT INTO `residence_histories` (`id`, `room_id`, `department_id`, `resident_name`, `check_in_date`, `check_out_date`, `is_responsible`, `remarks`, `version`, `created_at`, `updated_at`) VALUES
(1, 1, 1, '山田太郎',   '2026-04-01', NULL,         1, '責任者',       1, '2026-04-01 10:00:00', '2026-04-01 10:00:00'),
(2, 2, 2, '鈴木花子',   '2026-04-15', '2026-06-30', 0, NULL,           1, '2026-04-15 10:00:00', '2026-04-15 10:00:00'),
(3, 3, 3, '佐藤次郎',   '2026-03-01', '2026-06-15', 0, '転勤予定あり', 1, '2026-03-01 10:00:00', '2026-03-01 10:00:00'),
(4, 4, 1, '田中美咲',   '2026-05-01', NULL,         1, NULL,           1, '2026-05-01 10:00:00', '2026-05-01 10:00:00'),
(5, 5, 4, '渡辺健一',   '2026-02-01', '2026-06-10', 0, NULL,           1, '2026-02-01 10:00:00', '2026-02-01 10:00:00');

-- ============================================================
-- 6. residence_change_logs（変更履歴）
-- ============================================================
CREATE TABLE IF NOT EXISTS `residence_change_logs` (
  `id`                    INT                              NOT NULL AUTO_INCREMENT,
  `residence_history_id`  INT                              NOT NULL COMMENT '対象入居履歴ID',
  `operation_type`        ENUM('INSERT','UPDATE','DELETE') NOT NULL COMMENT '操作種別',
  `before_data`           JSON                             NULL     DEFAULT NULL COMMENT '変更前データ',
  `after_data`            JSON                             NULL     DEFAULT NULL COMMENT '変更後データ',
  `operated_by`           VARCHAR(100)                     NOT NULL COMMENT '操作者名',
  `operated_at`           DATETIME                         NOT NULL COMMENT '操作日時',
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_changelog_residence` FOREIGN KEY (`residence_history_id`) REFERENCES `residence_histories` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='変更履歴（3年以上保持）';

INSERT INTO `residence_change_logs` (`id`, `residence_history_id`, `operation_type`, `before_data`, `after_data`, `operated_by`, `operated_at`) VALUES
(1, 1, 'INSERT', NULL,
  '{"room_id":1,"resident_name":"山田太郎","check_in_date":"2026-04-01"}',
  '総務太郎', '2026-04-01 10:00:00'),
(2, 2, 'INSERT', NULL,
  '{"room_id":2,"resident_name":"鈴木花子","check_in_date":"2026-04-15","check_out_date":"2026-06-30"}',
  '総務太郎', '2026-04-15 10:00:00'),
(3, 3, 'UPDATE',
  '{"check_out_date":null}',
  '{"check_out_date":"2026-06-15"}',
  '人事次郎', '2026-05-10 14:00:00'),
(4, 4, 'INSERT', NULL,
  '{"room_id":4,"resident_name":"田中美咲","check_in_date":"2026-05-01"}',
  '総務太郎', '2026-05-01 10:00:00'),
(5, 5, 'UPDATE',
  '{"check_out_date":"2026-07-31"}',
  '{"check_out_date":"2026-06-10"}',
  '人事次郎', '2026-05-20 09:30:00');

-- ============================================================
-- 7. equipment（備品マスタ）※拡張対応
-- ============================================================
CREATE TABLE IF NOT EXISTS `equipment` (
  `id`         INT      NOT NULL AUTO_INCREMENT,
  `room_id`    INT      NULL     DEFAULT NULL COMMENT '対象部屋ID（転寮後はNULL許容）',
  `attributes` JSON     NOT NULL COMMENT '備品情報 {"ac":true,"ac_model":"型番"}',
  `version`    INT      NOT NULL DEFAULT 1 COMMENT '楽観ロック',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` DATETIME NULL     DEFAULT NULL COMMENT '論理削除',
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_equipment_room` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='備品マスタ（拡張対応）';

INSERT INTO `equipment` (`id`, `room_id`, `attributes`, `version`, `created_at`, `updated_at`) VALUES
(1, 1, '{"ac":true,"ac_model":"Panasonic CS-221CF"}',  1, '2026-04-01 09:00:00', '2026-04-01 09:00:00'),
(2, 2, '{"ac":true,"ac_model":"Daikin S22ATBS"}',      1, '2026-04-01 09:00:00', '2026-04-01 09:00:00'),
(3, 3, '{"ac":true,"ac_model":"Mitsubishi MSZ-GV2222"}',1,'2026-04-01 09:00:00', '2026-04-01 09:00:00'),
(4, 4, '{"ac":false,"ac_model":null}',                  1, '2026-04-01 09:00:00', '2026-04-01 09:00:00'),
(5, 5, '{"ac":true,"ac_model":"Hitachi RAS-AJ22L"}',   1, '2026-04-01 09:00:00', '2026-04-01 09:00:00');

-- ============================================================
-- 8. daily_supplies_costs（日用品費）※拡張対応
-- ============================================================
CREATE TABLE IF NOT EXISTS `daily_supplies_costs` (
  `id`           INT           NOT NULL AUTO_INCREMENT,
  `room_id`      INT           NOT NULL COMMENT '対象部屋ID',
  `target_year`  INT           NOT NULL COMMENT '対象年',
  `target_month` INT           NOT NULL COMMENT '対象月（1〜12）',
  `amount`       DECIMAL(10,2) NOT NULL COMMENT '金額（円）',
  `created_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at`   DATETIME      NULL     DEFAULT NULL COMMENT '論理削除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_room_year_month` (`room_id`, `target_year`, `target_month`),
  CONSTRAINT `fk_daily_supplies_room` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='日用品費（拡張対応）';

INSERT INTO `daily_supplies_costs` (`id`, `room_id`, `target_year`, `target_month`, `amount`, `created_at`, `updated_at`) VALUES
(1, 1, 2026, 4, 3500.00, '2026-05-01 09:00:00', '2026-05-01 09:00:00'),
(2, 2, 2026, 4, 3200.00, '2026-05-01 09:00:00', '2026-05-01 09:00:00'),
(3, 3, 2026, 4, 2800.00, '2026-05-01 09:00:00', '2026-05-01 09:00:00'),
(4, 4, 2026, 5, 3100.00, '2026-06-01 09:00:00', '2026-06-01 09:00:00'),
(5, 5, 2026, 3, 2500.00, '2026-04-01 09:00:00', '2026-04-01 09:00:00');

SET FOREIGN_KEY_CHECKS = 1;
-- ============================================================
-- 終了
-- ============================================================
