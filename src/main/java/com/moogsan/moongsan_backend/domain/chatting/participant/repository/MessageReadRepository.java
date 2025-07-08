package com.moogsan.moongsan_backend.domain.chatting.participant.repository;

import com.moogsan.moongsan_backend.domain.chatting.participant.entity.MessageReadDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MessageReadRepository extends MongoRepository<MessageReadDocument, String> {
}
