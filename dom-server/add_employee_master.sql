-- ============================================================
-- 社員寮管理システム — 社員マスタ追加スクリプト
-- モジュール：employee_master テーブル新規作成 + テストデータ投入
--             residence_histories の employee_id / gender / resident_name 更新
--             dormitories の dormitory_type 更新
-- 作成日：2026-06-12
-- 前提条件：
--   - full_seed.sql 実行済み（dormitories/rooms/residence_histories データあり）
--   - supplement.sql 実行済み（residence_histories.employee_id, gender カラム追加済み）
-- 実行方法：Navicat で直接実行（増量式、既存データをリセットしない）
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. employee_master テーブル新規作成
--    対象：自社社員（department_id=3）のみ登録
-- ============================================================

CREATE TABLE IF NOT EXISTS `employee_master` (
    `id`            INT           NOT NULL AUTO_INCREMENT COMMENT '主キーID',
    `employee_id`   CHAR(6)       NOT NULL COMMENT '社員番号（6桁数字）',
    `name`          VARCHAR(100)  NOT NULL COMMENT '氏名',
    `name_kana`     VARCHAR(100)  NULL     DEFAULT NULL COMMENT '氏名カナ',
    `gender`        ENUM('male','female') NOT NULL COMMENT '性別',
    `department_id` INT           NOT NULL COMMENT '所属ID（departments FK）',
    `status`        TINYINT       NOT NULL DEFAULT 1 COMMENT '在職状態（1=在職、0=退職）',
    `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
    `updated_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日時',
    `deleted_at`    DATETIME      NULL     DEFAULT NULL COMMENT '論理削除日時',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_employee_id` (`employee_id`),
    INDEX `idx_employee_department_id` (`department_id`),
    INDEX `idx_employee_status` (`status`),
    CONSTRAINT `fk_employee_department` FOREIGN KEY (`department_id`) REFERENCES `departments` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社員マスタ（自社社員のみ）';

-- ============================================================
-- 2. employee_master テストデータ（12件）
--    residence_histories の社員記録（department_id=3）に対応
--    対応関係：
--      id=4  (D)  → 田中大輔   male   100001
--      id=8  (H)  → 山田健一   male   100002
--      id=9  (I)  → 佐藤誠     male   100003
--      id=10 (J)  → 高橋翔     male   100004
--      id=11 (K)  → 鈴木美香   female 100005（退寮済）
--      id=18 (R)  → 伊藤由美   female 100006
--      id=23 (W)  → 渡辺浩二   male   100007
--      id=25 (Y)  → 中村麻衣   female 100008
--      id=27 (AA) → 小林哲也   male   100009
--      id=28 (AB) → 加藤誠司   male   100010
--      id=37 (AK) → 松本光子   female 100011
--      id=38 (AL) → 吉田拓也   male   100012
-- ============================================================

INSERT INTO `employee_master`
    (`employee_id`, `name`, `name_kana`, `gender`, `department_id`, `status`, `created_at`, `updated_at`)
VALUES
('100001', '田中大輔',   'タナカダイスケ', 'male',   3, 1, '2026-04-05 09:00:00', '2026-04-05 09:00:00'),
('100002', '山田健一',   'ヤマダケンイチ', 'male',   3, 1, '2024-07-04 09:00:00', '2024-07-04 09:00:00'),
('100003', '佐藤誠',     'サトウマコト',   'male',   3, 1, '2023-09-02 09:00:00', '2023-09-02 09:00:00'),
('100004', '高橋翔',     'タカハシショウ', 'male',   3, 1, '2023-12-30 09:00:00', '2023-12-30 09:00:00'),
('100005', '鈴木美香',   'スズキミカ',     'female', 3, 1, '2024-07-29 09:00:00', '2024-07-29 09:00:00'),
('100006', '伊藤由美',   'イトウユミ',     'female', 3, 1, '2025-06-09 09:00:00', '2025-06-09 09:00:00'),
('100007', '渡辺浩二',   'ワタナベコウジ', 'male',   3, 1, '2024-04-15 09:00:00', '2024-04-15 09:00:00'),
('100008', '中村麻衣',   'ナカムラマイ',   'female', 3, 1, '2025-08-27 09:00:00', '2025-08-27 09:00:00'),
('100009', '小林哲也',   'コバヤシテツヤ', 'male',   3, 1, '2023-03-27 09:00:00', '2023-03-27 09:00:00'),
('100010', '加藤誠司',   'カトウセイジ',   'male',   3, 1, '2025-03-25 09:00:00', '2025-03-25 09:00:00'),
('100011', '松本光子',   'マツモトミツコ', 'female', 3, 1, '2023-03-30 09:00:00', '2023-03-30 09:00:00'),
('100012', '吉田拓也',   'ヨシダタクヤ',   'male',   3, 1, '2020-08-06 09:00:00', '2020-08-06 09:00:00');

-- ============================================================
-- 3. residence_histories — 社員（department_id=3）の更新
--    employee_id / gender / resident_name を employee_master に合わせて設定
-- ============================================================

-- id=4 (D) → 田中大輔 male 100001
UPDATE `residence_histories`
SET `employee_id`   = '100001',
    `gender`        = 'male',
    `resident_name` = '田中大輔',
    `updated_at`    = CURRENT_TIMESTAMP
WHERE `id` = 4;

-- id=8 (H) → 山田健一 male 100002
UPDATE `residence_histories`
SET `employee_id`   = '100002',
    `gender`        = 'male',
    `resident_name` = '山田健一',
    `updated_at`    = CURRENT_TIMESTAMP
WHERE `id` = 8;

-- id=9 (I) → 佐藤誠 male 100003
UPDATE `residence_histories`
SET `employee_id`   = '100003',
    `gender`        = 'male',
    `resident_name` = '佐藤誠',
    `updated_at`    = CURRENT_TIMESTAMP
WHERE `id` = 9;

-- id=10 (J) → 高橋翔 male 100004
UPDATE `residence_histories`
SET `employee_id`   = '100004',
    `gender`        = 'male',
    `resident_name` = '高橋翔',
    `updated_at`    = CURRENT_TIMESTAMP
WHERE `id` = 10;

-- id=11 (K) → 鈴木美香 female 100005（退寮済）
UPDATE `residence_histories`
SET `employee_id`   = '100005',
    `gender`        = 'female',
    `resident_name` = '鈴木美香',
    `updated_at`    = CURRENT_TIMESTAMP
WHERE `id` = 11;

-- id=18 (R) → 伊藤由美 female 100006
UPDATE `residence_histories`
SET `employee_id`   = '100006',
    `gender`        = 'female',
    `resident_name` = '伊藤由美',
    `updated_at`    = CURRENT_TIMESTAMP
WHERE `id` = 18;

-- id=23 (W) → 渡辺浩二 male 100007
UPDATE `residence_histories`
SET `employee_id`   = '100007',
    `gender`        = 'male',
    `resident_name` = '渡辺浩二',
    `updated_at`    = CURRENT_TIMESTAMP
WHERE `id` = 23;

-- id=25 (Y) → 中村麻衣 female 100008
UPDATE `residence_histories`
SET `employee_id`   = '100008',
    `gender`        = 'female',
    `resident_name` = '中村麻衣',
    `updated_at`    = CURRENT_TIMESTAMP
WHERE `id` = 25;

-- id=27 (AA) → 小林哲也 male 100009
UPDATE `residence_histories`
SET `employee_id`   = '100009',
    `gender`        = 'male',
    `resident_name` = '小林哲也',
    `updated_at`    = CURRENT_TIMESTAMP
WHERE `id` = 27;

-- id=28 (AB) → 加藤誠司 male 100010
UPDATE `residence_histories`
SET `employee_id`   = '100010',
    `gender`        = 'male',
    `resident_name` = '加藤誠司',
    `updated_at`    = CURRENT_TIMESTAMP
WHERE `id` = 28;

-- id=37 (AK) → 松本光子 female 100011
UPDATE `residence_histories`
SET `employee_id`   = '100011',
    `gender`        = 'female',
    `resident_name` = '松本光子',
    `updated_at`    = CURRENT_TIMESTAMP
WHERE `id` = 37;

-- id=38 (AL) → 吉田拓也 male 100012
UPDATE `residence_histories`
SET `employee_id`   = '100012',
    `gender`        = 'male',
    `resident_name` = '吉田拓也',
    `updated_at`    = CURRENT_TIMESTAMP
WHERE `id` = 38;

-- ============================================================
-- 4. residence_histories — 派遣社員（department_id != 3）の更新
--    employee_id: D000001〜D000027（入寮日順に連番）
--    gender: 男女比 7:3 で設定（同一寮内に混在させる）
--    resident_name: 外国人カタカナ名に変更
--
--    派遣記録リスト（入寮日順 → 連番割り当て）：
--      D000001: id=13  M  dept=2  2019-01-05  豊洲I寮
--      D000002: id=12  L  dept=2  2020-07-01  豊洲I寮
--      D000003: id=9   ※社員のためスキップ → id=14
--      D000004: id=14  N  dept=2  2023-05-04  豊洲I寮
--
--    正しい入寮日順（派遣27件）：
--      id=13 (M)  2019-01-05  dept=2
--      id=12 (L)  2020-07-01  dept=2
--      id=14 (N)  2023-05-04  dept=2
--      id=35 (AI) 2025-03-02  dept=4
--      id=6  (F)  2025-06-30  dept=1（退寮済）
--      id=24 (X)  2026-03-07  dept=4
--      id=26 (Z)  2026-03-17  dept=1
--      id=15 (O)  2026-03-01  dept=1  ※←実際は2026-03-01
--      id=16 (P)  2026-03-01  dept=1  ※同日
--      id=20 (T)  2026-04-01  dept=1
--      id=22 (V)  2026-04-01  dept=1
--      id=29 (AC) 2026-03-01  dept=1（退寮済）
--      id=31 (AE) 2026-03-01  dept=1（退寮済）
--      id=30 (AD) 2026-04-07  dept=4
--      id=32 (AF) 2026-04-07  dept=4
--      id=33 (AG) 2026-04-07  dept=4
--      id=1  (A)  2025-11-30  dept=1（退寮済）
--      id=34 (AH) 2025-11-30  dept=1（退寮済）
--      id=3  (C)  2026-01-04  dept=1（退寮済）
--      id=36 (AJ) 2025-12-02  dept=1（退寮済）
--      id=5  (E)  2026-03-01  dept=1
--      id=7  (G)  2026-04-22  dept=1
--      id=17 (Q)  2026-03-30  dept=1
--      id=19 (S)  2026-02-23  dept=2
--      id=21 (U)  2026-01-31  dept=1（退寮済）
--      id=2  (B)  2024-08-30  dept=2
--      id=39 (AM) 2026-03-01  dept=4
--
--    最終的な連番割り当て（入寮日昇順）：
--      D000001: id=13 (M)  2019-01-05  male
--      D000002: id=12 (L)  2020-07-01  male
--      D000003: id=2  (B)  2024-08-30  male
--      D000004: id=35 (AI) 2025-03-02  male
--      D000005: id=1  (A)  2025-11-30  male（退寮済）
--      D000006: id=34 (AH) 2025-11-30  female（退寮済）
--      D000007: id=36 (AJ) 2025-12-02  male（退寮済）
--      D000008: id=3  (C)  2026-01-04  male（退寮済）
--      D000009: id=21 (U)  2026-01-31  female（退寮済）
--      D000010: id=19 (S)  2026-02-23  male
--      D000011: id=5  (E)  2026-03-01  male
--      D000012: id=15 (O)  2026-03-01  female  ← 大島C寮(female寮)
--      D000013: id=16 (P)  2026-03-01  female  ← 大島C寮(female寮)
--      D000014: id=29 (AC) 2026-03-01  male（退寮済）
--      D000015: id=31 (AE) 2026-03-01  male（退寮済）
--      D000016: id=39 (AM) 2026-03-01  male
--      D000017: id=24 (X)  2026-03-07  male
--      D000018: id=26 (Z)  2026-03-17  male
--      D000019: id=14 (N)  2023-05-04  male  ← 豊洲I寮(male寮)
--      D000020: id=17 (Q)  2026-03-30  female ← 大島C寮(female寮)
--      D000021: id=20 (T)  2026-04-01  male
--      D000022: id=22 (V)  2026-04-01  male
--      D000023: id=6  (F)  2025-06-30  male（退寮済）→ 豊洲D寮(male寮)
--      D000024: id=7  (G)  2026-04-22  male  ← 豊洲D寮(male寮)
--      D000025: id=30 (AD) 2026-04-07  male
--      D000026: id=32 (AF) 2026-04-07  male
--      D000027: id=33 (AG) 2026-04-07  male
--
--    ※ 連番の整合性のために id=14(N) はD000019として再配置
-- ============================================================

-- id=13 (M) → チャン・ウェイ male D000001（豊洲I寮 male寮）
UPDATE `residence_histories`
SET `employee_id`   = 'D000001',
    `gender`        = 'male',
    `resident_name` = 'チャン・ウェイ',
    `updated_at`    = CURRENT_TIMESTAMP
WHERE `id` = 13;

-- id=12 (L) → リン・ホン male D000002（豊洲I寮 male寮）
UPDATE `residence_histories`
SET `employee_id`   = 'D000002',
    `gender`        = 'male',
    `resident_name` = 'リン・ホン',
    `updated_at`    = CURRENT_TIMESTAMP
WHERE `id` = 12;

-- id=2 (B) → ワン・ミン male D000003（豊洲C寮 mixed寮）
UPDATE `residence_histories`
SET `employee_id`   = 'D000003',
    `gender`        = 'male',
    `resident_name` = 'ワン・ミン',
    `updated_at`    = CURRENT_TIMESTAMP
WHERE `id` = 2;

-- id=35 (AI) → リウ・ヤン male D000004（青砥A寮 mixed寮）
UPDATE `residence_histories`
SET `employee_id`   = 'D000004',
    `gender`        = 'male',
    `resident_name` = 'リウ・ヤン',
    `updated_at`    = CURRENT_TIMESTAMP
WHERE `id` = 35;

-- id=1 (A) → チェン・ジュン male D000005（豊洲C寮 退寮済）
UPDATE `residence_histories`
SET `employee_id`   = 'D000005',
    `gender`        = 'male',
    `resident_name` = 'チェン・ジュン',
    `updated_at`    = CURRENT_TIMESTAMP
WHERE `id` = 1;

-- id=34 (AH) → リー・ナ female D000006（青砥A寮 退寮済）
UPDATE `residence_histories`
SET `employee_id`   = 'D000006',
    `gender`        = 'female',
    `resident_name` = 'リー・ナ',
    `updated_at`    = CURRENT_TIMESTAMP
WHERE `id` = 34;

-- id=36 (AJ) → ジャン・リ male D000007（青砥A寮 退寮済）
UPDATE `residence_histories`
SET `employee_id`   = 'D000007',
    `gender`        = 'male',
    `resident_name` = 'ジャン・リ',
    `updated_at`    = CURRENT_TIMESTAMP
WHERE `id` = 36;

-- id=3 (C) → ウー・フェン male D000008（豊洲C寮 退寮済）
UPDATE `residence_histories`
SET `employee_id`   = 'D000008',
    `gender`        = 'male',
    `resident_name` = 'ウー・フェン',
    `updated_at`    = CURRENT_TIMESTAMP
WHERE `id` = 3;

-- id=21 (U) → ファン・シャオリ female D000009（大島E寮 退寮済）
UPDATE `residence_histories`
SET `employee_id`   = 'D000009',
    `gender`        = 'female',
    `resident_name` = 'ファン・シャオリ',
    `updated_at`    = CURRENT_TIMESTAMP
WHERE `id` = 21;

-- id=19 (S) → ソン・ハオ male D000010（大島D寮 female寮）
--   ※大島D寮はfemale寮に変更予定。Sはdepartment_id=2（派遣）。
--     性別制約テストのため female に設定する。
UPDATE `residence_histories`
SET `employee_id`   = 'D000010',
    `gender`        = 'female',
    `resident_name` = 'ジャン・メイ',
    `updated_at`    = CURRENT_TIMESTAMP
WHERE `id` = 19;

-- id=5 (E) → ルー・ウェイ male D000011（豊洲D寮 male寮）
UPDATE `residence_histories`
SET `employee_id`   = 'D000011',
    `gender`        = 'male',
    `resident_name` = 'ルー・ウェイ',
    `updated_at`    = CURRENT_TIMESTAMP
WHERE `id` = 5;

-- id=15 (O) → ツァン・リン female D000012（大島C寮 female寮）
UPDATE `residence_histories`
SET `employee_id`   = 'D000012',
    `gender`        = 'female',
    `resident_name` = 'ツァン・リン',
    `updated_at`    = CURRENT_TIMESTAMP
WHERE `id` = 15;

-- id=16 (P) → ウォン・シャオメイ female D000013（大島C寮 female寮）
UPDATE `residence_histories`
SET `employee_id`   = 'D000013',
    `gender`        = 'female',
    `resident_name` = 'ウォン・シャオメイ',
    `updated_at`    = CURRENT_TIMESTAMP
WHERE `id` = 16;

-- id=29 (AC) → ホー・チョン male D000014（大島H寮 退寮済）
UPDATE `residence_histories`
SET `employee_id`   = 'D000014',
    `gender`        = 'male',
    `resident_name` = 'ホー・チョン',
    `updated_at`    = CURRENT_TIMESTAMP
WHERE `id` = 29;

-- id=31 (AE) → ペン・ジアン male D000015（大島H寮 退寮済）
UPDATE `residence_histories`
SET `employee_id`   = 'D000015',
    `gender`        = 'male',
    `resident_name` = 'ペン・ジアン',
    `updated_at`    = CURRENT_TIMESTAMP
WHERE `id` = 31;

-- id=39 (AM) → マ・シャオロン male D000016（大阪D寮 mixed寮）
UPDATE `residence_histories`
SET `employee_id`   = 'D000016',
    `gender`        = 'male',
    `resident_name` = 'マ・シャオロン',
    `updated_at`    = CURRENT_TIMESTAMP
WHERE `id` = 39;

-- id=24 (X) → リェン・ジュオ male D000017（大島F寮 mixed寮）
UPDATE `residence_histories`
SET `employee_id`   = 'D000017',
    `gender`        = 'male',
    `resident_name` = 'リェン・ジュオ',
    `updated_at`    = CURRENT_TIMESTAMP
WHERE `id` = 24;

-- id=26 (Z) → ガオ・ミン male D000018（大島F寮 mixed寮）
UPDATE `residence_histories`
SET `employee_id`   = 'D000018',
    `gender`        = 'male',
    `resident_name` = 'ガオ・ミン',
    `updated_at`    = CURRENT_TIMESTAMP
WHERE `id` = 26;

-- id=14 (N) → スン・ボー male D000019（豊洲I寮 male寮）
UPDATE `residence_histories`
SET `employee_id`   = 'D000019',
    `gender`        = 'male',
    `resident_name` = 'スン・ボー',
    `updated_at`    = CURRENT_TIMESTAMP
WHERE `id` = 14;

-- id=17 (Q) → ドン・シューイン female D000020（大島C寮 female寮）
UPDATE `residence_histories`
SET `employee_id`   = 'D000020',
    `gender`        = 'female',
    `resident_name` = 'ドン・シューイン',
    `updated_at`    = CURRENT_TIMESTAMP
WHERE `id` = 17;

-- id=20 (T) → フー・レイ male D000021（大島E寮 mixed寮）
UPDATE `residence_histories`
SET `employee_id`   = 'D000021',
    `gender`        = 'male',
    `resident_name` = 'フー・レイ',
    `updated_at`    = CURRENT_TIMESTAMP
WHERE `id` = 20;

-- id=22 (V) → タン・ウェンジェ male D000022（大島E寮 mixed寮）
UPDATE `residence_histories`
SET `employee_id`   = 'D000022',
    `gender`        = 'male',
    `resident_name` = 'タン・ウェンジェ',
    `updated_at`    = CURRENT_TIMESTAMP
WHERE `id` = 22;

-- id=6 (F) → シェ・ユン male D000023（豊洲D寮 male寮 退寮済）
UPDATE `residence_histories`
SET `employee_id`   = 'D000023',
    `gender`        = 'male',
    `resident_name` = 'シェ・ユン',
    `updated_at`    = CURRENT_TIMESTAMP
WHERE `id` = 6;

-- id=7 (G) → ヘ・シャオポン male D000024（豊洲D寮 male寮）
UPDATE `residence_histories`
SET `employee_id`   = 'D000024',
    `gender`        = 'male',
    `resident_name` = 'ヘ・シャオポン',
    `updated_at`    = CURRENT_TIMESTAMP
WHERE `id` = 7;

-- id=30 (AD) → バイ・チャオ male D000025（大島H寮 mixed寮）
UPDATE `residence_histories`
SET `employee_id`   = 'D000025',
    `gender`        = 'male',
    `resident_name` = 'バイ・チャオ',
    `updated_at`    = CURRENT_TIMESTAMP
WHERE `id` = 30;

-- id=32 (AF) → ツォウ・ミン male D000026（大島H寮 mixed寮）
UPDATE `residence_histories`
SET `employee_id`   = 'D000026',
    `gender`        = 'male',
    `resident_name` = 'ツォウ・ミン',
    `updated_at`    = CURRENT_TIMESTAMP
WHERE `id` = 32;

-- id=33 (AG) → シュー・ジアシン male D000027（大島H寮 mixed寮）
UPDATE `residence_histories`
SET `employee_id`   = 'D000027',
    `gender`        = 'male',
    `resident_name` = 'シュー・ジアシン',
    `updated_at`    = CURRENT_TIMESTAMP
WHERE `id` = 33;

-- ============================================================
-- 5. dormitories の dormitory_type を更新
--    性別制約テスト用に特定の寮タイプを変更する
--    選定基準：現居住者が全員同性 or 全員退寮済み の寮
--
--    id=1 豊洲C寮：現居住者 D(100001,male) のみ → male
--    id=2 豊洲D寮：現居住者 E(D000011,male)、G(D000024,male)、H(100002,male) → male
--    id=3 豊洲H寮：現居住者 I(100003,male)、J(100004,male) → male
--    id=4 豊洲I寮：現居住者 L(D000002,male)、M(D000001,male)、N(D000019,male) → male
--    id=5 大島C寮：現居住者 O(D000012,female)、P(D000013,female)、Q(D000020,female) → female
--    id=6 大島D寮：現居住者 R(100006,female)、S(D000010,female) → female
--    id=7〜14：mixed のまま変更なし
-- ============================================================

UPDATE `dormitories` SET `dormitory_type` = 'male',   `updated_at` = CURRENT_TIMESTAMP WHERE `id` = 1;
UPDATE `dormitories` SET `dormitory_type` = 'male',   `updated_at` = CURRENT_TIMESTAMP WHERE `id` = 2;
UPDATE `dormitories` SET `dormitory_type` = 'male',   `updated_at` = CURRENT_TIMESTAMP WHERE `id` = 3;
UPDATE `dormitories` SET `dormitory_type` = 'male',   `updated_at` = CURRENT_TIMESTAMP WHERE `id` = 4;
UPDATE `dormitories` SET `dormitory_type` = 'female', `updated_at` = CURRENT_TIMESTAMP WHERE `id` = 5;
UPDATE `dormitories` SET `dormitory_type` = 'female', `updated_at` = CURRENT_TIMESTAMP WHERE `id` = 6;

-- ============================================================
-- 6. employee_master 追加社員データ（5件）
--    residence_histories に存在しない新規社員
--    employee_id: 100013〜100017
--    男性3名、女性2名
--    全員在職（status=1）
-- ============================================================

INSERT INTO `employee_master`
    (`employee_id`, `name`, `name_kana`, `gender`, `department_id`, `status`, `created_at`, `updated_at`)
VALUES
('100013', '木村大志',   'キムラタイシ',   'male',   3, 1, '2026-06-12 09:00:00', '2026-06-12 09:00:00'),
('100014', '村上さくら', 'ムラカミサクラ', 'female', 3, 1, '2026-06-12 09:00:00', '2026-06-12 09:00:00'),
('100015', '清水健太',   'シミズケンタ',   'male',   3, 1, '2026-06-12 09:00:00', '2026-06-12 09:00:00'),
('100016', '橋本奈緒',   'ハシモトナオ',   'female', 3, 1, '2026-06-12 09:00:00', '2026-06-12 09:00:00'),
('100017', '坂本賢二',   'サカモトケンジ', 'male',   3, 1, '2026-06-12 09:00:00', '2026-06-12 09:00:00');

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 終了
-- 実行後の確認クエリ（参考）：
--   SELECT COUNT(*) FROM employee_master;           -- 17件
--   SELECT * FROM employee_master ORDER BY employee_id;
--   SELECT id, employee_id, gender, resident_name FROM residence_histories ORDER BY id;
--   SELECT id, name, dormitory_type FROM dormitories WHERE id <= 6;
-- ============================================================
