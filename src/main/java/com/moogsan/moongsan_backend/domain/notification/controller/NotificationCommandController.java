package com.moogsan.moongsan_backend.domain.notification.controller;

import com.moogsan.moongsan_backend.domain.WrapperResponse;
import com.moogsan.moongsan_backend.domain.notification.dto.NotificationReadStatus;
import com.moogsan.moongsan_backend.domain.notification.dto.NotificationResponse;
import com.moogsan.moongsan_backend.domain.notification.dto.PagedResponse;
import com.moogsan.moongsan_backend.domain.notification.service.GetPastNotifications;
import com.moogsan.moongsan_backend.domain.notification.service.NotificationMarkAsRead;
import com.moogsan.moongsan_backend.domain.user.entity.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationCommandController {

    private final NotificationMarkAsRead notificationMarkAsRead;

    @PatchMapping("/{notificationId}")
    public ResponseEntity<WrapperResponse<PagedResponse<NotificationResponse>>> getPastNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long notificationId,
            @RequestBody NotificationReadStatus request) {

        notificationMarkAsRead.execute(userDetails.getUser().getId(), notificationId, request);

        return ResponseEntity.noContent().build();
    }
}
