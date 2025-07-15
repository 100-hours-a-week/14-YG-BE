package com.moogsan.moongsan_backend.groupbuy.domain.service;

import com.moogsan.moongsan_backend.domain.user.entity.User;
import com.moogsan.moongsan_backend.global.infrastructure.sse.SseEmitterRepository;
import com.moogsan.moongsan_backend.groupbuy.domain.dto.ChatSseResponse;
import com.moogsan.moongsan_backend.groupbuy.presentation.dto.command.request.ChatMessageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatBotService {
    private final AiClient aiClient;
    private final SseEmitterRepository sseEmitterRepository;

    public Mono<Void> streamChatForUser(User user,
                                                   ChatMessageRequest request,
                                                   String sessionId) {
        String key = "chat-bot:" + user.getId();

        // 1) processing 이벤트
        ChatSseResponse processingEvt = new ChatSseResponse(
                "processing",
                "분석 중...",
                "chat",
                Instant.now().atOffset(ZoneOffset.UTC).toString()
        );
        sseEmitterRepository.send(key, processingEvt);

        return aiClient.streamChat(user, request, sessionId)
                .doOnNext(chunk -> sseEmitterRepository.send(key, chunk))
                .then(Mono.fromRunnable(() ->
                        sseEmitterRepository.send(key, Map.of(
                                "type",      "completion",
                                "content",   "응답 완료",
                                "agent",     "chat",
                                "timestamp", Instant.now().atOffset(ZoneOffset.UTC).toString()
                        ))
                ));
    }
}
