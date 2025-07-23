-- V13__add_priority_level.sql

-- priority_level가상 컬럼 추가
-- 2 = 미읽음·ORDER_CANCELED
-- 1 = 미읽음·기타
-- 0 = 읽음
ALTER TABLE notification
  ADD COLUMN priority_level TINYINT GENERATED ALWAYS AS (
    CASE
      WHEN `read` = FALSE AND notification_type = 'ORDER_CANCELED' THEN 2
      WHEN `read` = FALSE THEN 1
      ELSE 0
    END
  ) STORED;

-- 2) 인덱스 (receiver_id, priority_level, id DESC)
CREATE INDEX idx_notif_rcv_pri_id
  ON notification (receiver_id, priority_level, id DESC);
