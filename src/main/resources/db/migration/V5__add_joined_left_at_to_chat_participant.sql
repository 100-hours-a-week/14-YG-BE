-- ============================================
-- Filename: V5__add_joined_left_at_to_chat_participant.sql
-- Purpose : chat_participant 테이블에 참가·퇴장 시각 컬럼 추가 (MySQL & H2 호환)
-- Encoding: UTF-8 (이모지, 한글 포함 대응)
-- ============================================

ALTER TABLE chat_participant
  ADD COLUMN joined_at TIMESTAMP NOT NULL
    DEFAULT CURRENT_TIMESTAMP
    COMMENT '채팅방 참가 시각'
    AFTER join_seq_no,
  ADD COLUMN left_at   TIMESTAMP NULL
    COMMENT '채팅방 퇴장 시각'
    AFTER joined_at;

-- 퇴장 시각으로 “현재 참여 중인” 레코드 조회 시 성능을 위해 인덱스 추가
CREATE INDEX idx_chat_participant_left_at
  ON chat_participant(left_at);
