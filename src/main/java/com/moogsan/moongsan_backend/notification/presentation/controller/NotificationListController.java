package com.moogsan.moongsan_backend.notification.presentation.controller;

import com.moogsan.moongsan_backend.global.dto.WrapperResponse;
import com.moogsan.moongsan_backend.notification.presentation.dto.NotificationResponse;
import com.moogsan.moongsan_backend.notification.presentation.dto.PagedResponse;
import com.moogsan.moongsan_backend.notification.application.service.GetPastNotifications;
import com.moogsan.moongsan_backend.domain.user.entity.CustomUserDetails;
import com.moogsan.moongsan_backend.global.exception.specific.UnauthenticatedAccessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.moogsan.moongsan_backend.notification.domain.message.ResponseMessage.GET_PAST_NOTIFICATION_SUCCESS;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationListController {

    private final GetPastNotifications getPastNotifications;

    @GetMapping("/latest")
    public ResponseEntity<WrapperResponse<PagedResponse<NotificationResponse>>> getPastNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(required = false, defaultValue = "10") int size
    ) {
        if (userDetails == null) throw new UnauthenticatedAccessException("로그인이 필요합니다.");

        PagedResponse<NotificationResponse> response = getPastNotifications
                .getPastNotifications(userDetails.getUser().getId(), cursorId, size);

        return ResponseEntity.ok(
                WrapperResponse.<PagedResponse<NotificationResponse>>builder()
                        .message(GET_PAST_NOTIFICATION_SUCCESS)
                        .data(response)
                        .build());
    }
}
