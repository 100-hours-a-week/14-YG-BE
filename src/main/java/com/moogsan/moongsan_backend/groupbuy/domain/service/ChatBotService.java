package com.moogsan.moongsan_backend.groupbuy.domain.service;

import com.moogsan.moongsan_backend.domain.user.entity.User;
import com.moogsan.moongsan_backend.global.infrastructure.sse.SseEmitterService;
import com.moogsan.moongsan_backend.groupbuy.domain.dto.ChatSseResponse;
import com.moogsan.moongsan_backend.groupbuy.presentation.dto.command.request.ChatMessageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatBotService {
    private final AiClient aiClient;
    private final SseEmitterService sseEmitterService;

    public Mono<Void> streamChatForUser(User user,
                                                   ChatMessageRequest request,
                                                   String sessionId) {
        String key = "chat:" + user.getId();

        // 1) processing 이벤트
        ChatSseResponse processingEvt = new ChatSseResponse(
                "processing",
                "분석 중...",
                "chat",
                Instant.now().atOffset(ZoneOffset.UTC).toString()
        );
        sseEmitterService.send(key, processingEvt);

        return aiClient.streamChat(user, request, sessionId)
                .doOnNext(chunk -> sseEmitterService.send(key, chunk))
                .then(Mono.fromRunnable(() ->
                        sseEmitterService.send(key, Map.of(
                                "type",      "completion",
                                "content",   "응답 완료",
                                "agent",     "chat",
                                "timestamp", Instant.now().atOffset(ZoneOffset.UTC).toString()
                        ))
                ));
    }
}
