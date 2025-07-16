package com.moogsan.moongsan_backend.domain.chatting.anonymous.entity;

import org.springframework.data.annotation.Id;
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
    private String messageId;

    @Field("post_id")
    private Long postId;

    @Field("participant_id")
    private Integer participantId;

    @Size(max = 150)
    @Field("message_content")
    private String messageContent;

    @Builder.Default
    private String type = "Normal";

    @Field("created_at")
    private LocalDateTime createdAt;
}
