package com.moogsan.moongsan_backend.domain.chatting_anon.controller;

import com.moogsan.moongsan_backend.domain.chatting_anon.dto.ChatAnonDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatAnonStompController {
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/{postId}")
    public void handleMessage(@DestinationVariable Long postId, ChatAnonDto message) {
        log.info("📨 메시지 수신: postId={}, aliasId={}, message={}",
                postId, message.getAliasId(), message.getMessage());

        messagingTemplate.convertAndSend("/topic/chat/" + postId, message);
    }
}
