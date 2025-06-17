-- V10__rename_pickup_change_reason.sql
-- Rename pickup_change_reason column to date_modification_reason
ALTER TABLE group_buy
  RENAME COLUMN pickup_change_reason TO date_modification_reason;