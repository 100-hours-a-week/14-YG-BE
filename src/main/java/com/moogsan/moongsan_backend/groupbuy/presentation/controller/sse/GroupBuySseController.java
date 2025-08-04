package com.moogsan.moongsan_backend.groupbuy.presentation.controller.sse;

import com.moogsan.moongsan_backend.global.infrastructure.sse.SseHub;
import com.moogsan.moongsan_backend.domain.user.entity.CustomUserDetails;
import com.moogsan.moongsan_backend.global.exception.specific.UnauthenticatedAccessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/group-buys")
public class GroupBuySseController {

    private final SseHub sseHub;

    @GetMapping(
            value = "/{groupBuyId}/sse",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter subscribe(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupBuyId) {

        if (userDetails == null) throw new UnauthenticatedAccessException("로그인이 필요합니다.");

        // 탭당 1개 연결을 열고, 기본(user:{userId}) + 상세(groupbuy:{id})를 초기 구독
        return sseHub.open(userDetails.getUser().getId(), Set.of("groupbuy:" + groupBuyId));
    }
}
