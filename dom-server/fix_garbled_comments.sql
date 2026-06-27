-- ============================================================
-- 文字化けカラムコメント修正スクリプト
-- 原因：supplement.sql 実行時に SET NAMES utf8mb4 が未設定のため
--       ALTER TABLE ADD COLUMN のコメントが latin1 で格納された
-- 対象：dormitories / equipment / residence_histories
-- 実行日：2026-06-06
-- ============================================================

SET NAMES utf8mb4;

-- ============================================================
-- 1. dormitories テーブル（1カラム）
-- ============================================================
ALTER TABLE `dormitories`
  MODIFY COLUMN `dormitory_type` VARCHAR(20) NULL DEFAULT 'mixed'
    COMMENT '寮タイプ(male/female/mixed)';

-- ============================================================
-- 2. equipment テーブル（5カラム）
-- ============================================================
ALTER TABLE `equipment`
  MODIFY COLUMN `name`          VARCHAR(100) NULL
    COMMENT '設備名称',
  MODIFY COLUMN `category`      VARCHAR(50)  NULL
    COMMENT '設備カテゴリ',
  MODIFY COLUMN `serial_number` VARCHAR(100) NULL
    COMMENT '管理番号',
  MODIFY COLUMN `status`        VARCHAR(20)  NOT NULL DEFAULT 'normal'
    COMMENT 'ステータス(normal/damaged/lost/in_storage)',
  MODIFY COLUMN `checkin_id`    INT          NULL
    COMMENT '入居記録ID';

-- ============================================================
-- 3. residence_histories テーブル（3カラム）
-- ============================================================
ALTER TABLE `residence_histories`
  MODIFY COLUMN `employee_id`            VARCHAR(50)  NULL
    COMMENT '社員番号',
  MODIFY COLUMN `gender`                 VARCHAR(10)  NULL
    COMMENT '性別(male/female)',
  MODIFY COLUMN `planned_checkout_date`  DATE         NULL
    COMMENT '退寮予定日';
