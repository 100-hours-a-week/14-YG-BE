package com.moogsan.moongsan_backend.notification.presentation.controller;

import com.moogsan.moongsan_backend.global.infrastructure.sse.SseEmitterRepository;
import com.moogsan.moongsan_backend.domain.user.entity.CustomUserDetails;
import com.moogsan.moongsan_backend.groupbuy.domain.service.AiClient;
import com.moogsan.moongsan_backend.groupbuy.presentation.dto.command.request.ChatMessageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationSseController {

    private final SseEmitterRepository sseEmitterRepository;

    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String key = "notification:" + userDetails.getUser().getId();
        return sseEmitterRepository.add(String.valueOf(key));
    }

}
