-- ============================================
-- Filename: V9_add_oauth_schema
-- Purpose : OAuth 테이블 생성
-- Encoding: UTF-8 (이모지, 한글 포함 대응)
-- ============================================

CREATE TABLE IF NOT EXISTS oauth (
  id                        BIGINT              NOT NULL AUTO_INCREMENT,
  user_id                   BIGINT              NOT NULL COMMENT '참조: users.id',
  provider                  VARCHAR(20)         NOT NULL COMMENT 'google, kakao, naver 등',
  provider_id               VARCHAR(50)         NOT NULL COMMENT '해당 OAuth 제공자에서의 고유 사용자 ID',
  refresh_token             VARCHAR(2048)       NOT NULL COMMENT '리프레시 토큰',
  refresh_token_expires_in  INT UNSIGNED        NOT NULL COMMENT '리프레시 토큰 만료까지 남은 초',
  PRIMARY KEY (id),
  INDEX idx_oauth_user (user_id),
  CONSTRAINT fk_oauth_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COMMENT='OAuth 연동 정보 테이블';