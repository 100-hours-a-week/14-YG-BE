-- V7__add_unique_constraint_to_chat_room.sql

-- 1) group_buy_id 컬럼용 단일-컬럼 인덱스 추가 (존재하지 않을 때만)
SET @has_single_idx = (
  SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
   WHERE TABLE_SCHEMA = DATABASE()
     AND TABLE_NAME   = 'chat_room'
     AND INDEX_NAME   = 'idx_chat_room_group_buy'
);
SET @add_single_sql = IF(
  @has_single_idx = 0,
  'ALTER TABLE `chat_room` ADD INDEX `idx_chat_room_group_buy` (`group_buy_id`)',
  'SELECT 1'
);
PREPARE stmt FROM @add_single_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2) 기존 복합 UNIQUE 인덱스가 있으면 DROP (없으면 스킵)
SET @has_idx = (
  SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
   WHERE TABLE_SCHEMA = DATABASE()
     AND TABLE_NAME   = 'chat_room'
     AND INDEX_NAME   = 'uq_chat_room_group_buy_room_type'
);
SET @drop_sql = IF(
  @has_idx > 0,
  'ALTER TABLE `chat_room` DROP INDEX `uq_chat_room_group_buy_room_type`',
  'SELECT 1'
);
PREPARE stmt FROM @drop_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3) (새) 복합 UNIQUE 인덱스 추가 (존재하지 않을 때만)
SET @has_unique_idx = (
  SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
   WHERE TABLE_SCHEMA = DATABASE()
     AND TABLE_NAME   = 'chat_room'
     AND INDEX_NAME   = 'uq_chat_room_group_buy_room_type'
);
SET @add_unique_sql = IF(
  @has_unique_idx = 0,
  'ALTER TABLE `chat_room` ADD UNIQUE INDEX `uq_chat_room_group_buy_room_type` (`group_buy_id`, `type`)',
  'SELECT 1'
);
PREPARE stmt FROM @add_unique_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
