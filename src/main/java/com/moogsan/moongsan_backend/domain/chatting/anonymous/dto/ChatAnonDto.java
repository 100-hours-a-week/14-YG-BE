package com.moogsan.moongsan_backend.domain.chatting.anonymous.dto;

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
    private long postId;
    private int aliasId;
    private String message;
    private LocalDateTime createdAt;

    public static ChatAnonDto from(ChatAnon entity){
        return new ChatAnonDto(
                entity.getPostId(),
                entity.getAliasId(),
                entity.getMessage(),
                entity.getCreatedAt()
        );
    }

    public ChatAnon toEntity() {
        return ChatAnon.builder()
                .postId(this.postId)
                .aliasId(this.aliasId)
                .message(this.message)
                .build();
    }
}
