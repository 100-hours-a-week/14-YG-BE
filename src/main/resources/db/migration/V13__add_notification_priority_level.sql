-- V13__add_notification_priority_level.sql  (수정본)
ALTER TABLE notification
  -- 1) 먼저 notification_type 컬럼을 만든다 (IF NOT EXISTS 제거!)
  ADD COLUMN notification_type VARCHAR(20) NOT NULL
    COMMENT 'INFO | WARN | ERROR',
  -- 2) 그 다음 priority_level 생성 컬럼
  ADD COLUMN priority_level TINYINT GENERATED ALWAYS AS (
        CASE notification_type
          WHEN 'INFO'  THEN 1
          WHEN 'WARN'  THEN 2
          WHEN 'ERROR' THEN 3
        END
  ) STORED,
  -- 3) 인덱스
  ADD INDEX idx_notification_priority (priority_level);
