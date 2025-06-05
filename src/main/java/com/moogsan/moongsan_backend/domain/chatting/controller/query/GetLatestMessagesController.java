package com.moogsan.moongsan_backend.domain.chatting.controller.query;

import com.moogsan.moongsan_backend.domain.WrapperResponse;
import com.moogsan.moongsan_backend.domain.chatting.Facade.query.ChattingQueryFacade;
import com.moogsan.moongsan_backend.domain.chatting.dto.command.response.CommandChattingReponse;
import com.moogsan.moongsan_backend.domain.chatting.dto.query.ChatMessageResponse;
import com.moogsan.moongsan_backend.domain.chatting.service.query.GetLatestMessages;
import com.moogsan.moongsan_backend.domain.user.entity.CustomUserDetails;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Collections;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chats/participant")
public class GetLatestMessagesController {

    private final ChattingQueryFacade chattingQueryFacade;

    @GetMapping("/{chatRoomId}/polling/latest")
    public DeferredResult<WrapperResponse<List<ChatMessageResponse>>> getLatestMessages(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long chatRoomId,
            @RequestParam(required = false) String lastMessageId
    ) {
        // -- (A) wrapperResult 타임아웃: rawResult보다 +1초 여유를 둠
        DeferredResult<WrapperResponse<List<ChatMessageResponse>>> wrapperResult =
                new DeferredResult<>(31_000L);

        // -- (B) Service로부터 rawResult 획득 (여긴 30초 타임아웃)
        DeferredResult<List<ChatMessageResponse>> rawResult =
                chattingQueryFacade.getLatesetMessages(userDetails.getUser(), chatRoomId, lastMessageId);

        // ─────────────────────────────────────────────────────────────────────────────
        // (1) rawResult가 정상적으로 메시지를 반환할 때
        rawResult.setResultHandler(rawValue -> {
            @SuppressWarnings("unchecked")
            List<ChatMessageResponse> messages = (List<ChatMessageResponse>) rawValue;

            // wrapperResult에 성공 포맷 세팅
            wrapperResult.setResult(
                    WrapperResponse.<List<ChatMessageResponse>>builder()
                            .message("최신 메세지를 성공적으로 수신하였습니다. <<long polling>>")
                            .data(messages)
                            .build()
            );
        });

        // (2) rawResult에서 타임아웃이 발생했을 때
        rawResult.onTimeout(() -> {
            // rawResult는 더 이상 값을 주지 못하므로, 우리가 wrapperResult에 '타임아웃 응답' 세팅
            wrapperResult.setResult(
                    WrapperResponse.<List<ChatMessageResponse>>builder()
                            .message("타임아웃: 새로운 메시지가 없습니다.")
                            .data(Collections.emptyList())
                            .build()
            );
        });

        // (3) rawResult 에러 발생 시 (예: DB 오류 등)
        rawResult.onError(error -> {
            wrapperResult.setErrorResult(
                    WrapperResponse.<List<ChatMessageResponse>>builder()
                            .message("에러 발생: " + error.getMessage())
                            .data(Collections.emptyList())
                            .build()
            );
        });

        // ─────────────────────────────────────────────────────────────────────────────
        // (4) wrapperResult 자체가 타임아웃 되는 경우(= rawResult가 어떤 이유로 callback을 안 했을 때)
        // 새로운 메세지가 없는 상황이라고 가정
        wrapperResult.onTimeout(() -> {
            wrapperResult.setResult(
                    WrapperResponse.<List<ChatMessageResponse>>builder()
                            .message("타임아웃: 새로운 메시지가 없습니다.")
                            .data(Collections.emptyList())
                            .build()
            );
        });

        // (5) wrapperResult 자체에서 에러가 터질 경우
        wrapperResult.onError(err -> {
            wrapperResult.setErrorResult(
                    WrapperResponse.<List<ChatMessageResponse>>builder()
                            .message("WrapperResult 에러: " + err.getMessage())
                            .data(Collections.emptyList())
                            .build()
            );
        });

        return wrapperResult;
    }
}