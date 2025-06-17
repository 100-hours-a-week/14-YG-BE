-- ============================================
-- Filename: V5__add_joined_left_at_to_chat_participant.sql
-- Purpose : chat_participant 테이블에 참가·퇴장 시각 컬럼 추가 (MySQL & H2 호환)
-- Encoding: UTF-8 (이모지, 한글 포함 대응)
-- ============================================

-- 컬럼이 존재하지 않을 때만 추가
SET @has_joined_at = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'chat_participant'
      AND COLUMN_NAME = 'joined_at'
);

SET @has_left_at = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'chat_participant'
      AND COLUMN_NAME = 'left_at'
);

SET @sql = IF(@has_joined_at = 0, 'ALTER TABLE chat_participant ADD COLUMN joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT ''채팅방 참가 시각'' AFTER join_seq_no', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(@has_left_at = 0, 'ALTER TABLE chat_participant ADD COLUMN left_at TIMESTAMP NULL COMMENT ''채팅방 퇴장 시각'' AFTER joined_at', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 인덱스가 존재하지 않을 때만 추가
SET @has_index = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'chat_participant'
      AND INDEX_NAME = 'idx_chat_participant_left_at'
);

SET @sql = IF(@has_index = 0, 'CREATE INDEX idx_chat_participant_left_at ON chat_participant(left_at)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
