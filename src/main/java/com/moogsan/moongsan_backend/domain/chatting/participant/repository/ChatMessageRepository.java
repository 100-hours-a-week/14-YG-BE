package com.moogsan.moongsan_backend.domain.chatting.participant.repository;

import com.moogsan.moongsan_backend.domain.chatting.participant.entity.ChatMessageDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessageDocument, String> {
    @Query("{ 'chatRoomId': ?0, '_id': { $gt: ?1 } }")
    List<ChatMessageDocument> findMessagesAfter(Long chatRoomId, String lastMessageId);

    ChatMessageDocument findTopByChatRoomIdOrderByCreatedAtDesc(Long chatRoomId);
}
