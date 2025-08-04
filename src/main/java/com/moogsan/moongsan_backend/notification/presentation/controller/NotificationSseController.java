package com.moogsan.moongsan_backend.notification.presentation.controller;

import com.moogsan.moongsan_backend.domain.user.entity.CustomUserDetails;
import com.moogsan.moongsan_backend.global.infrastructure.sse.SseHub;
import com.moogsan.moongsan_backend.global.infrastructure.sse.SubRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications") // 당분간 프론트엔드와 통일
public class NotificationSseController {

    private final SseHub sseHub;

    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return sseHub.open(userDetails.getUser().getId(), null);
    }

    @PutMapping("/sse/subscriptions")
    public void update(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody SubRequest request
    ) {
        sseHub.updateSubscriptions(userDetails.getUser().getId(), request.getConnectionId(), request.getAdd(), request.getRemove());
    }

}
