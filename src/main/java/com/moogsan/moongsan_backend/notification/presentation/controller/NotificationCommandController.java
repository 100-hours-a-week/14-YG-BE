package com.moogsan.moongsan_backend.notification.presentation.controller;

import com.moogsan.moongsan_backend.global.dto.WrapperResponse;
import com.moogsan.moongsan_backend.global.security.annotation.RequireLogin;
import com.moogsan.moongsan_backend.notification.presentation.dto.NotificationReadStatus;
import com.moogsan.moongsan_backend.notification.presentation.dto.NotificationResponse;
import com.moogsan.moongsan_backend.notification.presentation.dto.PagedResponse;
import com.moogsan.moongsan_backend.notification.application.service.NotificationMarkAsRead;
import com.moogsan.moongsan_backend.domain.user.entity.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
@RequireLogin
public class NotificationCommandController {

    private final NotificationMarkAsRead notificationMarkAsRead;

    @PatchMapping("/{notificationId}")
    public ResponseEntity<WrapperResponse<PagedResponse<NotificationResponse>>> getPastNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long notificationId,
            @RequestBody NotificationReadStatus request) {

        notificationMarkAsRead.execute(notificationId, request);

        return ResponseEntity.noContent().build();
    }
}
