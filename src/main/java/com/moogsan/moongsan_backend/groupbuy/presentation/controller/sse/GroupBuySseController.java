package com.moogsan.moongsan_backend.groupbuy.presentation.controller.sse;

import com.moogsan.moongsan_backend.adapters.kafka.producer.publisher.GroupBuyRealtime;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/group-buys")
public class GroupBuySseController {

    private final GroupBuyRealtime groupBuyRealtime;

    @GetMapping(
            value = "/{groupBuyId}/sse",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter subscribe(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupBuyId) {

        if (userDetails == null) throw new UnauthenticatedAccessException("로그인이 필요합니다.");

        return groupBuyRealtime.subscribe(groupBuyId);
    }
}
