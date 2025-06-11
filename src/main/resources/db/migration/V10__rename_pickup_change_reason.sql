-- V10__rename_pickup_change_reason.sql

ALTER TABLE group_buy
RENAME COLUMN pickup_change_reason TO date_modification_reason;
