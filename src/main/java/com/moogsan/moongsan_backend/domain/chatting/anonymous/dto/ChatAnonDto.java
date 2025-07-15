package com.moogsan.moongsan_backend.domain.chatting.anonymous.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.moogsan.moongsan_backend.domain.chatting.anonymous.entity.ChatAnon;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatAnonDto {
    @JsonProperty("messageId")
    private String messageId;

    @JsonProperty("postId")
    private Long postId;

    @JsonProperty("participantId")
    private Integer participantId;

    @JsonProperty("messageContent")
    private String messageContent;

    @JsonProperty("type")
    private String type;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    public static ChatAnonDto from(ChatAnon entity) {
        return new ChatAnonDto(
                entity.getMessageId(),
                entity.getPostId(),
                entity.getParticipantId(),
                entity.getMessageContent(),
                entity.getType(),
                entity.getCreatedAt()
        );
    }

    public ChatAnon toEntity() {
        return ChatAnon.builder()
                .messageId(this.messageId)
                .postId(this.postId)
                .participantId(this.participantId)
                .messageContent(this.messageContent)
                .type(this.type)
                .createdAt(this.createdAt)
                .build();
    }
}
