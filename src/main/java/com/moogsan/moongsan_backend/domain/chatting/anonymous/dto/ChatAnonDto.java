package com.moogsan.moongsan_backend.domain.chatting.anonymous.dto;

import com.moogsan.moongsan_backend.domain.chatting.anonymous.entity.ChatAnon;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ChatAnonDto {
    private Long postId;
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
                .createdAt(this.createdAt != null ? this.createdAt : LocalDateTime.now())
                .build();
    }
}
