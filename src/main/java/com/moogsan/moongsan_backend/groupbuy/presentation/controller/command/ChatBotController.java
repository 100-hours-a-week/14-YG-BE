package com.moogsan.moongsan_backend.groupbuy.presentation.controller.command;

import com.moogsan.moongsan_backend.domain.user.entity.CustomUserDetails;
import com.moogsan.moongsan_backend.global.infrastructure.sse.SseEmitterRepository;
import com.moogsan.moongsan_backend.groupbuy.domain.dto.ChatSseResponse;
import com.moogsan.moongsan_backend.groupbuy.domain.service.AiClient;
import com.moogsan.moongsan_backend.groupbuy.domain.service.ChatBotService;
import com.moogsan.moongsan_backend.groupbuy.presentation.dto.command.request.ChatMessageRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;

import static com.moogsan.moongsan_backend.global.util.CookieUtils.extractCookie;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat-bot")
public class ChatBotController {

    private final SseEmitterRepository sseEmitterRepository;
    private final ChatBotService chatBotService;

    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String key = "chat:" + userDetails.getUser().getId();
        return sseEmitterRepository.add(String.valueOf(key));
    }

    @PostMapping("/message")
    public Mono<Void> sendMessage(@AuthenticationPrincipal CustomUserDetails userDetails,
                                  @RequestBody ChatMessageRequest request,
                                  HttpServletRequest servletRequest) {
        String sessionId = extractCookie(servletRequest, "AccessToken");
        return chatBotService.streamChatForUser(
                        userDetails.getUser(),
                        request,
                        sessionId
                )
                .then();
    }

}
