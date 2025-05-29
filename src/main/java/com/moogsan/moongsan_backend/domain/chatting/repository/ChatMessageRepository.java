package com.moogsan.moongsan_backend.domain.chatting.repository;

import com.moogsan.moongsan_backend.domain.chatting.entity.ChatMessageDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessageDocument, String> {
    List<ChatMessageDocument> findByChatRoomIdAndMessageSeqGreaterThanOrderByMessageSeqAsc(Long roomId, Long lastMessageId);
}
