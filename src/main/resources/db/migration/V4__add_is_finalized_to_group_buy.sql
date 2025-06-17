-- ============================================
-- Filename: V4__add_is_finalized_to_group_buy.sql
-- Purpose : group_buy 테이블에 공구 체결 여부 컬럼 추가 (MySQL & H2 호환)
-- Encoding: UTF-8 (이모지, 한글 포함 대응)
-- ============================================

-- 컬럼이 존재하지 않을 때만 추가
SET @has_is_finalized = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'group_buy'
      AND COLUMN_NAME = 'is_finalized'
);

SET @sql = IF(@has_is_finalized = 0, 'ALTER TABLE group_buy ADD COLUMN is_finalized BOOLEAN DEFAULT FALSE', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
