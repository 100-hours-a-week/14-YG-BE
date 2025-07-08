CREATE TABLE notification (
    id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    receiver_id       BIGINT UNSIGNED NOT NULL,
    title             VARCHAR(200)  NOT NULL,
    body              TEXT          NOT NULL,
    notification_type VARCHAR(50)   NOT NULL,
    data              JSON          NULL,
    created_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at           DATETIME      NULL,
    `read`            BIT(1)        NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);

CREATE INDEX idx_notification_user_unread
    ON notification (receiver_id, `read`, created_at DESC);
