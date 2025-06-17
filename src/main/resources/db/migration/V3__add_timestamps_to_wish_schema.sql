-- ============================================
-- Filename: V3__add_timestamps_to_wish_schema.sql
-- Purpose : wish 테이블에 생성일, 수정일, 삭제일 컬럼 추가 (MySQL & H2 호환)
-- Encoding: UTF-8 (이모지, 한글 포함 대응)
-- ============================================

ALTER TABLE wish
ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN deleted_at TIMESTAMP NULL;