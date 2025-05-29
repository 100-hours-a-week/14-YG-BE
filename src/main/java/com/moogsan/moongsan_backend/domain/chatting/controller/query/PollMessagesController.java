package com.moogsan.moongsan_backend.domain.chatting.controller.query;

import com.moogsan.moongsan_backend.domain.chatting.Facade.query.ChattingQueryFacade;
import com.moogsan.moongsan_backend.domain.chatting.Facade.query.ChattingQueryFacadeImpl;
import com.moogsan.moongsan_backend.domain.chatting.dto.query.ChatMessagePageResponse;
import com.moogsan.moongsan_backend.domain.chatting.dto.query.ChatMessageResponse;
import com.moogsan.moongsan_backend.domain.user.entity.CustomUserDetails;
import com.moogsan.moongsan_backend.global.exception.specific.UnauthenticatedAccessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.concurrent.DelegatingSecurityContextRunnable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat-rooms")
public class PollMessagesController {

    private final ChattingQueryFacade chattingQueryFacade;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    @GetMapping("/{chatRoomId}/polling")
    public DeferredResult<ResponseEntity<ChatMessagePageResponse>> pollMessages(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("chatRoomId") Long chatRoomId,
            @RequestParam(required = false, defaultValue = "0") String cursorId
    ) {

        if (userDetails == null) throw new UnauthenticatedAccessException("로그인이 필요합니다.");

        log.info("Polling for messages after id={}", cursorId);

        DeferredResult<ResponseEntity<ChatMessagePageResponse>> result =
                new DeferredResult<>(30_000L, ResponseEntity.noContent().build());

        result.onTimeout(() ->
                log.warn("롱폴링 타임아웃 chatRoomId={} lastMessageId={}", chatRoomId, cursorId)
        );

        result.onError((err) ->
                log.error("롱폴링 처리 중 에러", err)
        );

        executor.submit(new DelegatingSecurityContextRunnable(() -> {
            try {
                while (!result.isSetOrExpired()) {
                    ChatMessagePageResponse response = chattingQueryFacade
                            .pollMessages(userDetails.getUser(), chatRoomId, cursorId);
                    if (!response.getChatMessageResponses().isEmpty()) {
                        log.info("📤 응답 반환 준비 완료: {}개 메시지",
                                response.getChatMessageResponses().size());

                        result.setResult(ResponseEntity.ok(response));
                        break;
                    }
                    Thread.sleep(1_000);
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                ChatMessagePageResponse empty = ChatMessagePageResponse.builder()
                        .chatMessageResponses(Collections.emptyList())
                        .hasNext(false)
                        .build();
                result.setErrorResult(ResponseEntity.status(500).body(empty));
            } catch (Exception ex) {
                log.error("롱폴링 처리 오류", ex);
                ChatMessagePageResponse empty = ChatMessagePageResponse.builder()
                        .chatMessageResponses(Collections.emptyList())
                        .hasNext(false)
                        .build();
                result.setErrorResult(ResponseEntity.status(500).body(empty));
            }
        }));
        return result;
    }
}
