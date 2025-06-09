package com.moogsan.moongsan_backend.domain.chatting.controller.query;

import com.moogsan.moongsan_backend.domain.WrapperResponse;
import com.moogsan.moongsan_backend.domain.chatting.Facade.query.ChattingQueryFacade;
import com.moogsan.moongsan_backend.domain.chatting.dto.query.ChatMessageResponse;
import com.moogsan.moongsan_backend.domain.chatting.service.query.GetLatestMessageSse;
import com.moogsan.moongsan_backend.domain.user.entity.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chats/participant")
public class GetLatestMessagesSseController {
    private final ChattingQueryFacade chattingQueryFacade;

    @GetMapping("/{chatRoomId}/sse/latest")
    public SseEmitter getLatestMessagesSse(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long chatRoomId
    ) {
        return chattingQueryFacade.getLatestMessagesSse(userDetails.getUser(), chatRoomId);
        // 1) DB 조회 · 검증 (트랜잭션 경계 안에서만 수행)
        // getLatestMessageSse.validateParticipant(userDetails.getUser(), chatRoomId);

        // 2) 검증 이후, 완전히 트랜잭션 닫힌 다음에야 Emitter 생성
        // return getLatestMessageSse.createEmitter(chatRoomId);
    }
}
