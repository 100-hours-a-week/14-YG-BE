package com.moogsan.moongsan_backend.participantchat.domain.repository;

import com.moogsan.moongsan_backend.participantchat.domain.entity.MessageReadDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MessageReadRepository extends MongoRepository<MessageReadDocument, String> {
}
