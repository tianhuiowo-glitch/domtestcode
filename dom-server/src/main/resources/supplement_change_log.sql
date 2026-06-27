-- 入居履歴変更ログテーブル作成
-- 実行前にデータベースを選択してください: USE your_database_name;

CREATE TABLE IF NOT EXISTS residence_change_logs (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    residence_history_id INT          NOT NULL COMMENT '入居履歴ID（residence_histories.id）',
    operation_type       VARCHAR(10)  NOT NULL COMMENT '操作タイプ（INSERT/UPDATE/DELETE）',
    operated_by          VARCHAR(100) COMMENT '操作者ユーザー名',
    operated_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作時刻',
    resident_name        VARCHAR(100) COMMENT '入居者氏名',
    dormitory_name       VARCHAR(100) COMMENT '宿舍名',
    room_name            VARCHAR(100) COMMENT '部屋名'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '入居履歴変更ログ';
