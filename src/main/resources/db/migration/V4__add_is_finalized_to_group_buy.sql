-- ============================================
-- Filename: V4__add_is_finalized_to_group_buy.sql
-- Purpose : group_buy 테이블에 공구 체결 여부 컬럼 추가 (MySQL & H2 호환)
-- Encoding: UTF-8 (이모지, 한글 포함 대응)
-- ============================================

ALTER TABLE group_buy
ADD COLUMN is_finalized BOOLEAN DEFAULT FALSE;
