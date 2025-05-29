-- V7__add_unique_constraint_to_chat_room.sql
-- 고유 제약: 동일 공구(group_buy_id)-room_type 조합은 한 번만 생성

-- 0) 저장 프로시저 블록 안에서만 IF문 사용 가능하므로,
--    여기서는 직접 정보스키마를 조회해 동적 SQL을 생성합니다.

-- 1) group_buy_id 단일 인덱스가 없으면 추가
SET @cnt = (
  SELECT COUNT(*) FROM information_schema.statistics
   WHERE table_schema = DATABASE()
     AND table_name   = 'chat_room'
     AND index_name   = 'idx_chat_room_group_buy'
);
IF @cnt = 0 THEN
  ALTER TABLE chat_room
    ADD INDEX idx_chat_room_group_buy (group_buy_id);
END IF;

-- 2) old 복합 UNIQUE 인덱스가 있으면 지우기
SET @cnt = (
  SELECT COUNT(*) FROM information_schema.statistics
   WHERE table_schema = DATABASE()
     AND table_name   = 'chat_room'
     AND index_name   = 'uq_chat_room_group_buy_room_type'
);
IF @cnt = 1 THEN
  ALTER TABLE chat_room
    DROP INDEX uq_chat_room_group_buy_room_type;
END IF;

-- 3) 새 UNIQUE 제약(인덱스)이 없으면 추가
SET @cnt = (
  SELECT COUNT(*) FROM information_schema.statistics
   WHERE table_schema = DATABASE()
     AND table_name   = 'chat_room'
     AND index_name   = 'uq_chat_room_group_buy_room_type'
);
IF @cnt = 0 THEN
  ALTER TABLE chat_room
    ADD CONSTRAINT uq_chat_room_group_buy_room_type
      UNIQUE (group_buy_id, type);
END IF;