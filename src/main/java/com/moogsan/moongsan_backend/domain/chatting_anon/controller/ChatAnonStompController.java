package com.moogsan.moongsan_backend.domain.chatting_anon.controller;

import com.moogsan.moongsan_backend.domain.chatting_anon.dto.ChatAnonDto;
import com.moogsan.moongsan_backend.domain.chatting_anon.service.SendChatAnonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatAnonStompController {

    private final SendChatAnonService sendChatAnonService;

    @MessageMapping("/chat/{postId}")
    public void handleMessage(@DestinationVariable Long postId, @Payload ChatAnonDto message) {
        sendChatAnonService.processMessage(postId, message.getAliasId(), message);
    }
}
