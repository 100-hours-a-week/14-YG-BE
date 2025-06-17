-- ============================================
-- Filename: V2__add_chat_schema.sql
-- Purpose : 채팅 도메인 관련 테이블 추가 (MySQL & H2 호환)
-- Encoding: UTF-8 (이모지, 한글 포함 대응)
-- ============================================

-- 1. 채팅방 테이블
CREATE TABLE chat_room (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT NOT NULL COMMENT '참조: group_buy.id',
    type VARCHAR(20) NOT NULL COMMENT '채팅방 유형 (anonymous, participant, dm)',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '채팅방 활성 여부',
    participants_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '참여 인원 수',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    INDEX idx_chat_room_post (post_id),
    FOREIGN KEY (post_id) REFERENCES group_buy(id) ON DELETE CASCADE
);

-- 2. 채팅방 참가자 테이블
CREATE TABLE chat_participant (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    chat_room_id BIGINT NOT NULL COMMENT '참조: chat_room.id',
    user_id BIGINT NOT NULL COMMENT '참조: users.id',
    join_seq_no INT UNSIGNED DEFAULT NULL COMMENT '익명 채팅방 참여 순서',
    INDEX idx_chat_participant_room (chat_room_id),
    INDEX idx_chat_participant_user (user_id),
    FOREIGN KEY (chat_room_id) REFERENCES chat_room(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 3. 채팅 메시지 테이블 (MySQL에서 테스트용으로 운영 예정)
CREATE TABLE chat_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    participant_id BIGINT NOT NULL COMMENT '참조: chat_participant.id',
    chat_room_id BIGINT NOT NULL COMMENT '참조: chat_room.id',
    content VARCHAR(4000) NOT NULL COMMENT '메시지 내용 (최대 4000자)',
    view_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '읽은 인원 수',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '전송 시각',
    INDEX idx_chat_message_participant (participant_id),
    INDEX idx_chat_message_room (chat_room_id),
    FOREIGN KEY (participant_id) REFERENCES chat_participant(id) ON DELETE CASCADE,
    FOREIGN KEY (chat_room_id) REFERENCES chat_room(id) ON DELETE CASCADE
);

-- 4. 메시지 읽음 테이블 (MySQL에서 테스트 후 MongoDB로 이전 예정)
CREATE TABLE message_read (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    participant_id BIGINT NOT NULL COMMENT '참조: chat_participant.id',
    message_id BIGINT NOT NULL COMMENT '참조: chat_message.id',
    read_at TIMESTAMP NULL COMMENT '읽은 시각',
    INDEX idx_message_read_participant (participant_id),
    INDEX idx_message_read_message (message_id),
    FOREIGN KEY (participant_id) REFERENCES chat_participant(id) ON DELETE CASCADE,
    FOREIGN KEY (message_id) REFERENCES chat_message(id) ON DELETE CASCADE
);
