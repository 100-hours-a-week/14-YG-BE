package com.moogsan.moongsan_backend.domain.chatting.repository;

import com.moogsan.moongsan_backend.domain.chatting.entity.MessageReadDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MessageReadRepository extends MongoRepository<MessageReadDocument, String> {
}
