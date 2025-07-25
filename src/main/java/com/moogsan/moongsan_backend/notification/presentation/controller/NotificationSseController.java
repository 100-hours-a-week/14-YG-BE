package com.moogsan.moongsan_backend.notification.presentation.controller;

import com.moogsan.moongsan_backend.global.infrastructure.sse.SseEmitterService;
import com.moogsan.moongsan_backend.domain.user.entity.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static com.moogsan.moongsan_backend.notification.domain.constant.NotificationConstants.NOTI_SSE_PREFIX;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationSseController {

    private final SseEmitterService sseEmitterService;

    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String key = NOTI_SSE_PREFIX + userDetails.getUser().getId();
        return sseEmitterService.add(String.valueOf(key));
    }

}
