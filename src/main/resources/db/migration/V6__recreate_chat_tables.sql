-- V6__recreate_chat_tables.sql

-- 1) 기존 테이블 제거
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS message_read CASCADE;
DROP TABLE IF EXISTS chat_messages CASCADE;
DROP TABLE IF EXISTS chat_participant CASCADE;
DROP TABLE IF EXISTS chat_room CASCADE;
SET FOREIGN_KEY_CHECKS = 1;

-- 2) chat_room 테이블 생성
CREATE TABLE chat_room (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(255),
    participants_count INT NOT NULL DEFAULT 0,
    group_buy_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_chat_room_group_buy
        FOREIGN KEY (group_buy_id)
        REFERENCES group_buy (id)
);

-- 3) chat_participant 테이블 생성
CREATE TABLE chat_participant (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    join_seq_no INT NOT NULL DEFAULT 0,
    joined_at DATETIME NOT NULL,
    left_at DATETIME,
    chat_room_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_chat_participant_chat_room
        FOREIGN KEY (chat_room_id)
        REFERENCES chat_room (id),
    CONSTRAINT fk_chat_participant_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
);
