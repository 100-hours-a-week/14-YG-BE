-- V8__add_soft_delete_and_modified_to_chat_room.sql

ALTER TABLE chat_room
  ADD COLUMN deleted_at DATETIME NULL AFTER created_at,
  ADD COLUMN modified_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER group_buy_id;
