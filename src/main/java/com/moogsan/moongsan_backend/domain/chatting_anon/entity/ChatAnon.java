package com.moogsan.moongsan_backend.domain.chatting_anon.entity;

import jakarta.persistence.Id;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Document(collection = "chat_message_anon")
public class ChatAnon {
    @Id
    private String id;

    private Long postId;
    private int aliasId;

    @Size(max = 150)
    private String message;

    private LocalDateTime createdAt;
}
