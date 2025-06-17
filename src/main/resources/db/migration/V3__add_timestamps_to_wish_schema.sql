-- ============================================
-- Filename: V3__add_timestamps_to_wish_schema.sql
-- Purpose : wish 테이블에 생성일, 수정일, 삭제일 컬럼 추가 (MySQL & H2 호환)
-- Encoding: UTF-8 (이모지, 한글 포함 대응)
-- ============================================

-- 컬럼이 존재하지 않을 때만 추가
SET @has_created_at = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'wish'
      AND COLUMN_NAME = 'created_at'
);

SET @has_modified_at = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'wish'
      AND COLUMN_NAME = 'modified_at'
);

SET @has_deleted_at = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'wish'
      AND COLUMN_NAME = 'deleted_at'
);

SET @sql = IF(@has_created_at = 0, 'ALTER TABLE wish ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(@has_modified_at = 0, 'ALTER TABLE wish ADD COLUMN modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(@has_deleted_at = 0, 'ALTER TABLE wish ADD COLUMN deleted_at TIMESTAMP NULL', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;