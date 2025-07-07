package com.moogsan.moongsan_backend.domain.notification.controller;

import com.moogsan.moongsan_backend.adapters.sse.SseEmitterRepository;
import com.moogsan.moongsan_backend.domain.user.entity.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sse")
public class NotificationSseController {

    private final SseEmitterRepository sseEmitterRepository;

    @GetMapping(path = "/notifications", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return sseEmitterRepository.add(String.valueOf(userDetails.getUser().getId()));
    }

}
