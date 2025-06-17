-- ============================================
-- Filename: V1__init_schema.sql
-- Purpose : H2 & MySQL 호환 초기 테이블 정의
-- Encoding: UTF-8 (이모지, 한글 포함 대응)
-- ============================================

-- H2는 SET NAMES / ENGINE / CHARSET 등을 이해하지 못하므로 제거
-- 외래키 체크는 H2에선 무시됨 (Flyway 실행 오류 방지를 위해 주석 처리)
-- SET NAMES utf8mb4;
-- SET FOREIGN_KEY_CHECKS = 0;

-- 1. 유저 테이블
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    nickname VARCHAR(60),
    name VARCHAR(36),
    phone_number VARCHAR(11),
    password VARCHAR(60),
    account_bank VARCHAR(30),
    account_number VARCHAR(32),
    image_key VARCHAR(512),
    type VARCHAR(30) NOT NULL DEFAULT 'USER',
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    logout_at TIMESTAMP,
    deleted_at TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_status (status),
    INDEX idx_deleted_at (deleted_at)
);

-- 2. 토큰 테이블
CREATE TABLE refresh_token (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token VARCHAR(512) NOT NULL,
    expires TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_token (token)
);

-- 3. 카테고리 테이블
CREATE TABLE category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(60) NOT NULL
);

-- 4. 공동구매 게시글 테이블
CREATE TABLE group_buy (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(400) NOT NULL,
    name VARCHAR(400) NOT NULL,
    url TEXT,
    price INT NOT NULL,
    unit_price INT NOT NULL,
    total_amount INT NOT NULL,
    left_amount INT NOT NULL,
    unit_amount INT NOT NULL,
    host_quantity INT NOT NULL,
    description VARCHAR(1500) NOT NULL,
    due_soon BOOLEAN NOT NULL DEFAULT FALSE,
    badge VARCHAR(60),
    due_date TIMESTAMP NOT NULL,
    location VARCHAR(300) NOT NULL,
    pickup_date TIMESTAMP NOT NULL,
    wish_count INT NOT NULL DEFAULT 0,
    view_count INT NOT NULL DEFAULT 0,
    participant_count INT NOT NULL DEFAULT 0,
    post_status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    pickup_change_reason VARCHAR(765),
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 5. 공구글-카테고리 매핑 테이블
CREATE TABLE group_buy_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_id BIGINT NOT NULL,
    group_buy_id BIGINT NOT NULL,
    FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE CASCADE,
    FOREIGN KEY (group_buy_id) REFERENCES group_buy(id) ON DELETE CASCADE,
    UNIQUE (category_id, group_buy_id)
);

-- 6. 이미지 테이블
CREATE TABLE image (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    image_key VARCHAR(765) NOT NULL,
    image_resized_key VARCHAR(765),
    image_seq_no INT NOT NULL DEFAULT 0,
    thumbnail BOOLEAN NOT NULL DEFAULT FALSE,
    group_buy_id BIGINT NOT NULL,
    FOREIGN KEY (group_buy_id) REFERENCES group_buy(id) ON DELETE CASCADE
);

-- 7. 주문 테이블
CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    price INT NOT NULL,
    quantity INT NOT NULL,
    name VARCHAR(50) NOT NULL,
    deleted_count INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (post_id) REFERENCES group_buy(id)
);

-- 8. 위시(관심) 테이블
CREATE TABLE wish (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (post_id) REFERENCES group_buy(id) ON DELETE CASCADE,
    UNIQUE (user_id, post_id),
    INDEX idx_user_id (user_id),
    INDEX idx_post_id (post_id)
);

-- 외래키 체크 복구는 H2에선 필요 없음
-- SET FOREIGN_KEY_CHECKS = 1;