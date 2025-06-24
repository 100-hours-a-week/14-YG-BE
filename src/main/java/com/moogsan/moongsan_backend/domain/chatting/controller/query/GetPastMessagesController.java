package com.moogsan.moongsan_backend.domain.chatting.controller.query;

import com.moogsan.moongsan_backend.domain.WrapperResponse;
import com.moogsan.moongsan_backend.domain.chatting.Facade.query.ChattingQueryFacade;
import com.moogsan.moongsan_backend.domain.chatting.dto.command.response.CommandChattingReponse;
import com.moogsan.moongsan_backend.domain.chatting.dto.query.ChatMessagePageResponse;
import com.moogsan.moongsan_backend.domain.user.entity.CustomUserDetails;
import com.moogsan.moongsan_backend.global.exception.specific.UnauthenticatedAccessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.concurrent.DelegatingSecurityContextRunnable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chats/participant")
public class GetPastMessagesController {

    private final ChattingQueryFacade chattingQueryFacade;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    @GetMapping("/{chatRoomId}/message/past")
    public ResponseEntity<WrapperResponse<ChatMessagePageResponse>> getPastMessages(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("chatRoomId") Long chatRoomId,
            @RequestParam(required = false) String cursorMessageIdBefore,
            @RequestParam(required = false) String cursorMessageIdAfter
    ) {

        if (cursorMessageIdBefore == null && cursorMessageIdAfter == null) {
            // 최초 요청 → 최신 메시지부터 시작
            return ResponseEntity.ok(
                    WrapperResponse.<ChatMessagePageResponse>builder()
                            .message("최신 메시지 조회 성공")
                            .data(chattingQueryFacade.getPastMessages(userDetails.getUser(), chatRoomId, null, true)) // 최신 → 과거 방향
                            .build());
        }

        if (cursorMessageIdBefore != null && cursorMessageIdAfter != null) {
            throw new IllegalArgumentException("하나의 커서만 제공해야 합니다.");
        }

        boolean isPrev = cursorMessageIdAfter != null;
        String cursorId = isPrev ? cursorMessageIdAfter : cursorMessageIdBefore;


        ChatMessagePageResponse response = chattingQueryFacade.getPastMessages(userDetails.getUser(), chatRoomId, cursorId, isPrev);

        return ResponseEntity.ok(
                WrapperResponse.<ChatMessagePageResponse>builder()
                        .message("과거 메세지 리스트를 성공적으로 조회하였습니다.")
                        .data(response)
                        .build());
    }
}
