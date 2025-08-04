-- V13__add_notification_priority_level.sql  (수정본)
ALTER TABLE notification
  ADD COLUMN priority_level TINYINT GENERATED ALWAYS AS (
        CASE notification_type
          WHEN 'INFO'  THEN 1
          WHEN 'WARN'  THEN 2
          WHEN 'ERROR' THEN 3
        END
  ) STORED,
  ADD INDEX idx_notification_priority (priority_level);
