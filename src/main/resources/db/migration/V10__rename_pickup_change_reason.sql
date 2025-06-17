-- V10__rename_pickup_change_reason.sql
-- Rename pickup_change_reason column to date_modification_reason (컬럼이 존재할 때만)

SET @has_old_column = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'group_buy'
      AND COLUMN_NAME = 'pickup_change_reason'
);

SET @has_new_column = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'group_buy'
      AND COLUMN_NAME = 'date_modification_reason'
);

SET @sql = IF(@has_old_column > 0 AND @has_new_column = 0, 'ALTER TABLE group_buy RENAME COLUMN pickup_change_reason TO date_modification_reason', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;