-- ============================================================
-- コメント修正スクリプト：中国語コメント → 日本語コメント
-- 対象：supplement.sql / fix_remaining.sql で作成したテーブル＋カラム
-- 実行日：2026-06-06
-- ============================================================

SET NAMES utf8mb4;

-- ============================================================
-- 1. equipment テーブル（supplement.sql 一にて追加したカラム）
-- ============================================================
ALTER TABLE `equipment`
  MODIFY COLUMN `dormitory_id` INT NULL COMMENT '寮ID';

-- ============================================================
-- 2. sys_users テーブル（全カラム＋テーブルコメント）
-- ============================================================
ALTER TABLE `sys_users`
  MODIFY COLUMN `id`            BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主キーID',
  MODIFY COLUMN `username`      VARCHAR(50)  NOT NULL                  COMMENT 'ユーザー名',
  MODIFY COLUMN `password`      VARCHAR(255) NOT NULL                  COMMENT 'パスワード（BCrypt暗号化）',
  MODIFY COLUMN `real_name`     VARCHAR(100) NULL                      COMMENT '氏名',
  MODIFY COLUMN `role`          VARCHAR(30)  NOT NULL DEFAULT 'operator' COMMENT 'ロール(admin/operator)',
  MODIFY COLUMN `status`        TINYINT      NOT NULL DEFAULT 1        COMMENT 'ステータス(0:無効 1:有効)',
  MODIFY COLUMN `last_login_at` DATETIME     NULL                      COMMENT '最終ログイン日時',
  MODIFY COLUMN `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
  MODIFY COLUMN `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日時',
  MODIFY COLUMN `deleted_at`    DATETIME     NULL DEFAULT NULL         COMMENT '削除日時',
  COMMENT = 'システムユーザーテーブル';

-- admin の氏名を日本語に修正
UPDATE `sys_users` SET `real_name` = 'システム管理者' WHERE `username` = 'admin';

-- ============================================================
-- 3. fees テーブル（混在カラム修正）
-- ============================================================
ALTER TABLE `fees`
  MODIFY COLUMN `id`             BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '主キーID',
  MODIFY COLUMN `residence_id`   INT           NOT NULL                  COMMENT '入居履歴ID',
  MODIFY COLUMN `dormitory_id`   INT           NULL                      COMMENT '寮ID',
  MODIFY COLUMN `dormitory_name` VARCHAR(100)  NULL                      COMMENT '寮名',
  MODIFY COLUMN `room_id`        INT           NULL                      COMMENT '部屋ID',
  MODIFY COLUMN `room_name`      VARCHAR(50)   NULL                      COMMENT '部屋名',
  MODIFY COLUMN `created_at`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
  MODIFY COLUMN `updated_at`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日時',
  MODIFY COLUMN `deleted_at`     DATETIME      NULL DEFAULT NULL         COMMENT '削除日時';

-- ============================================================
-- 4. operation_logs テーブル
-- ============================================================
ALTER TABLE `operation_logs`
  MODIFY COLUMN `id`          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主キーID',
  MODIFY COLUMN `username`    VARCHAR(100) NOT NULL                  COMMENT '操作ユーザー名',
  MODIFY COLUMN `action`      VARCHAR(100) NOT NULL                  COMMENT '操作アクション',
  MODIFY COLUMN `resource`    VARCHAR(100) NULL                      COMMENT '操作対象リソース',
  MODIFY COLUMN `resource_id` VARCHAR(50)  NULL                      COMMENT 'リソースID',
  MODIFY COLUMN `detail`      TEXT         NULL                      COMMENT '操作詳細（JSON）',
  MODIFY COLUMN `ip_address`  VARCHAR(50)  NULL                      COMMENT '操作元IPアドレス',
  MODIFY COLUMN `status`      VARCHAR(20)  NOT NULL DEFAULT 'success' COMMENT '操作結果(success/fail)',
  MODIFY COLUMN `operated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作日時';

-- ============================================================
-- 5. equipment_processes テーブル
-- ============================================================
ALTER TABLE `equipment_processes`
  MODIFY COLUMN `id`           BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '主キーID',
  MODIFY COLUMN `equipment_id` INT           NOT NULL                  COMMENT '設備ID',
  MODIFY COLUMN `checkin_id`   INT           NULL                      COMMENT '入居履歴ID',
  MODIFY COLUMN `process_type` VARCHAR(20)   NOT NULL                  COMMENT '処理タイプ(damaged/lost)',
  MODIFY COLUMN `description`  TEXT          NULL                      COMMENT '処理内容',
  MODIFY COLUMN `cost`         DECIMAL(10,2) NULL                      COMMENT '処理費用',
  MODIFY COLUMN `status`       VARCHAR(20)   NOT NULL DEFAULT 'pending' COMMENT 'ステータス(pending/completed)',
  MODIFY COLUMN `completed_at` DATETIME      NULL                      COMMENT '完了日時',
  MODIFY COLUMN `created_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
  MODIFY COLUMN `updated_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日時',
  MODIFY COLUMN `deleted_at`   DATETIME      NULL DEFAULT NULL         COMMENT '削除日時';

-- ============================================================
-- 6. equipment_storage テーブル
-- ============================================================
ALTER TABLE `equipment_storage`
  MODIFY COLUMN `id`               BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主キーID',
  MODIFY COLUMN `equipment_id`     INT          NOT NULL                  COMMENT '設備ID',
  MODIFY COLUMN `equipment_name`   VARCHAR(100) NULL                      COMMENT '設備名',
  MODIFY COLUMN `category`         VARCHAR(50)  NULL                      COMMENT '設備分類',
  MODIFY COLUMN `storage_location` VARCHAR(100) NULL                      COMMENT '保管場所',
  MODIFY COLUMN `stored_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '入庫日時',
  MODIFY COLUMN `remarks`          TEXT         NULL                      COMMENT '備考',
  MODIFY COLUMN `created_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
  MODIFY COLUMN `updated_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日時',
  MODIFY COLUMN `deleted_at`       DATETIME     NULL DEFAULT NULL         COMMENT '削除日時';
