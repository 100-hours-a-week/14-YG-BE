CREATE TABLE outbox_event (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  event_id CHAR(36) NOT NULL UNIQUE,
  aggregate_type VARCHAR(64) NOT NULL,
  aggregate_id VARCHAR(64) NOT NULL,
  kafka_topic VARCHAR(100) NOT NULL,
  kafka_partition_key VARCHAR(100),
  payload JSON NOT NULL,
  headers JSON,
  status ENUM('PENDING', 'PUBLISHED', 'FAILED', 'DEAD') NOT NULL DEFAULT 'PENDING',
  retry_count TINYINT UNSIGNED DEFAULT 0,
  next_retry_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  published_at TIMESTAMP DEFAULT NULL,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  KEY idx_status_created (status, created_at),
  KEY idx_retry (status, next_retry_at),
  KEY idx_aggregate (aggregate_type, aggregate_id)
);
