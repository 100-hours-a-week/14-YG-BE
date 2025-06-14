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
            @RequestParam(required = false) String cursorId
    ) {

        ChatMessagePageResponse response = chattingQueryFacade.getPastMessages(userDetails.getUser(), chatRoomId, cursorId);

        return ResponseEntity.ok(
                WrapperResponse.<ChatMessagePageResponse>builder()
                        .message("과거 메세지 리스트를 성공적으로 조회하였습니다.")
                        .data(response)
                        .build());
    }
}
