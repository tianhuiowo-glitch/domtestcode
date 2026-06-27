-- ============================================================
-- パッチ R-01: equipment.room_id NOT NULL 制約の撤廃
-- 作成日: 2026-06-27
-- 背景: 備品転寮機能（PUT /api/v1/equipment/{id}/transfer）実装により
--       転寮後に room_id = NULL（部屋未割当）をセットする必要が生じた。
--       転寮後の備品は新しい寮に属するが部屋割は後から決定されるため
--       NULL 許容が正しい設計。
-- 影響テーブル: equipment
-- 前提: テスト環境では ALTER TABLE 済み（2026-06-27実施）
-- 本番適用: 本番DB適用前に必ず実行すること
-- ============================================================

USE dormitory_db;

-- room_id を NULL 許容に変更
ALTER TABLE `equipment`
  MODIFY COLUMN `room_id` INT NULL DEFAULT NULL COMMENT '対象部屋ID（転寮後はNULL許容）';

-- 適用確認用クエリ（コメント）
-- SHOW COLUMNS FROM equipment LIKE 'room_id';
-- 期待結果: Null = YES, Default = NULL
