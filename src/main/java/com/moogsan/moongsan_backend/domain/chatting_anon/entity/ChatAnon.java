package com.moogsan.moongsan_backend.domain.chatting_anon.entity;

import jakarta.persistence.Id;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Getter
@Builder
@Document(collection = "chat_messages_anon")
public class ChatAnon {
    @Id
    private String id;

    @Field("post_id")
    private Long postId;
    @Field("alias_id")
    private int aliasId;

    @Size(max = 150)
    private String message;

    @Field("created_at")
    private LocalDateTime createdAt;
}
