package com.moogsan.moongsan_backend.domain.chatting.anonymous.entity;

import lombok.Setter;
import org.springframework.data.annotation.Id;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Getter
@Setter
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
    private boolean isSafe = true;

    @Builder.Default
    @Field("blur_reason")
    private String blurReason = null;

    @Field("created_at")
    private LocalDateTime createdAt;

    public void setIsSafe(boolean isSafe) {this.isSafe = isSafe;}
}
