-- V8__add_soft_delete_and_modified_to_chat_room.sql

-- 컬럼이 존재하지 않을 때만 추가
SET @has_deleted_at = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'chat_room'
      AND COLUMN_NAME = 'deleted_at'
);

SET @has_modified_at = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'chat_room'
      AND COLUMN_NAME = 'modified_at'
);

SET @sql = IF(@has_deleted_at = 0, 'ALTER TABLE chat_room ADD COLUMN deleted_at DATETIME NULL AFTER created_at', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(@has_modified_at = 0, 'ALTER TABLE chat_room ADD COLUMN modified_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER group_buy_id', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
