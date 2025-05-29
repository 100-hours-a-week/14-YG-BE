-- V7__add_unique_constraint_to_chat_room.sql
-- 고유 제약: 동일 공구(group_buy_id)-room_type 조합은 한 번만 생성되도록 한다.

-- 1) 외래키(group_buy_id)용 단일-컬럼 인덱스 추가
ALTER TABLE chat_room
  ADD INDEX idx_chat_room_group_buy (group_buy_id);

-- 2) 기존 복합 UNIQUE 제약(인덱스) 삭제
ALTER TABLE chat_room
  DROP INDEX uq_chat_room_group_buy_room_type;

 -- 3) UNIQUE 제약(인덱스)을 다시 추가합니다.
 ALTER TABLE chat_room
   ADD CONSTRAINT uq_chat_room_group_buy_room_type
     UNIQUE (group_buy_id, type);
