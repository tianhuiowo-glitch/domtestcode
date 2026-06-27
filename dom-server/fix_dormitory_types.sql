-- ============================================================
-- 社員寮管理システム — 寮タイプ修正スクリプト
-- 目的：全14件の dormitory_type を 'mixed' から 'male' / 'female' に変更し、
--       関連する residence_histories.gender および employee_master.gender を合わせる
-- 割り当て：
--   male  寮 : id = 1, 2, 3, 4, 5, 8, 9, 11, 12
--   female寮 : id = 6, 7, 10, 13, 14
-- 前提条件：full_seed.sql および add_employee_master.sql 実行済み
-- 実行方法：Navicat で直接実行（実行後ロールバック不可）
-- 作成日：2026-06-12
-- ============================================================

SET NAMES utf8mb4;

-- ============================================================
-- 1. dormitories.dormitory_type を更新
-- ============================================================

-- 男性寮に設定（id = 1, 2, 3, 4, 5, 8, 9, 11, 12）
UPDATE dormitories SET dormitory_type = 'male' WHERE id IN (1, 2, 3, 4, 5, 8, 9, 11, 12) AND deleted_at IS NULL;

-- 女性寮に設定（id = 6, 7, 10, 13, 14）
UPDATE dormitories SET dormitory_type = 'female' WHERE id IN (6, 7, 10, 13, 14) AND deleted_at IS NULL;

-- ============================================================
-- 2. residence_histories.gender を寮タイプに合わせて更新
--    rooms.dormitory_id を経由して、各居住記録の性別を更新
-- ============================================================

-- 男性寮の入居者 → gender = 'male'
UPDATE residence_histories rh
INNER JOIN rooms rm ON rm.id = rh.room_id AND rm.deleted_at IS NULL
SET rh.gender = 'male'
WHERE rm.dormitory_id IN (1, 2, 3, 4, 5, 8, 9, 11, 12)
  AND rh.deleted_at IS NULL;

-- 女性寮の入居者 → gender = 'female'
UPDATE residence_histories rh
INNER JOIN rooms rm ON rm.id = rh.room_id AND rm.deleted_at IS NULL
SET rh.gender = 'female'
WHERE rm.dormitory_id IN (6, 7, 10, 13, 14)
  AND rh.deleted_at IS NULL;

-- ============================================================
-- 3. employee_master.gender を居住記録の寮タイプに合わせて更新
--    employee_id が一致する場合のみ更新
-- ============================================================

-- employee_master の性別を residence_histories の寮ベースの性別で上書き
UPDATE employee_master em
INNER JOIN (
    SELECT rh.employee_id, rh.gender
    FROM residence_histories rh
    WHERE rh.deleted_at IS NULL
      AND rh.employee_id IS NOT NULL
      AND rh.employee_id != ''
) rh_gender ON rh_gender.employee_id = em.employee_id
SET em.gender = rh_gender.gender
WHERE em.deleted_at IS NULL;
