package com.moogsan.moongsan_backend.domain.chatting.participant.controller.websocket;

import com.moogsan.moongsan_backend.domain.chatting.participant.facade.query.ChattingQueryFacade;
import com.moogsan.moongsan_backend.domain.chatting.participant.dto.query.request.ChatStompRequest;
import com.moogsan.moongsan_backend.domain.user.entity.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class GetLatestMessagesStompController {

    private final ChattingQueryFacade chattingQueryFacade;

    @MessageMapping("/api/chats/participant/message")
    public void handleMessage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Payload ChatStompRequest payload
    ) {
        chattingQueryFacade.getLatestMessagesStomp(userDetails.getUser(), payload.getChatRoomId(), payload.getLastMessageId());
    }
}
