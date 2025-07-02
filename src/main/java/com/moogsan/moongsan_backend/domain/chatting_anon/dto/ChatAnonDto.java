package com.moogsan.moongsan_backend.domain.chatting_anon.dto;

import com.moogsan.moongsan_backend.domain.chatting_anon.entity.ChatAnon;
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
    private int aliasId;
    private String message;
    private LocalDateTime createdAt;

    public static ChatAnonDto from(ChatAnon entity){
        return new ChatAnonDto(
                entity.getAliasId(),
                entity.getMessage(),
                entity.getCreatedAt()
        );
    }
}
