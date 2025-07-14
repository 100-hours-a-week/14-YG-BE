package com.moogsan.moongsan_backend.domain.chatting.participant.dto.query.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatStompRequest {

    private Long chatRoomId;
    private String lastMessageId;

}
