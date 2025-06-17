-- V10__rename_modification_reason.sql
-- MySQL
ALTER TABLE group_buy
  RENAME COLUMN pickup_change_reason TO date_modification_reason;

-- H2
-- H2는 RENAME COLUMN 대신 아래 구문을 사용해야 합니다
ALTER TABLE group_buy
  ALTER COLUMN pickup_change_reason RENAME TO date_modification_reason;