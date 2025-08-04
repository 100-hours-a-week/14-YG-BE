package com.moogsan.moongsan_backend.global.infrastructure.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 1) Chat, 주문, 시스템 알림용 공용 SSE 저장소
 * 2) key = 수신 대상(예: userId, chatRoomId 등) / value = emitter list
 * 3) 만료·끊김 시 자동 제거
 */
@Slf4j
@Component
public class SseEmitterService {

    private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();
    private static final long DEFAULT_TIMEOUT = 130_000L; // 130초

    /** 신규 연결 등록 */
    public SseEmitter add(String key) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        emitters
                .computeIfAbsent(key, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(emitter);

        // 콜백 등록
        emitter.onCompletion(() -> remove(key, emitter));
        emitter.onTimeout(() -> {
            log.debug("⏱ SSE timeout, remove emitter → key={}, emitter={}", key, emitter);
            emitter.complete();
            remove(key, emitter);
        });
        emitter.onError(e -> {
            log.debug("⚠️ SSE error, remove emitter → key={}, emitter={}", key, emitter, e);
            emitter.completeWithError(e);
            remove(key, emitter);
        });

        try {
            log.debug("➕ SSE 구독 등록 → key={}, totalEmitters={}", key, emitters.get(key).size());
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("SSE 연결 성공"));
        } catch (IOException e) {
            log.warn("Failed to send connect event, remove emitter → key={}, emitter={}", key, emitter, e);
            remove(key, emitter);
        }

        return emitter;
    }

    /** 대상 키에 연결된 모든 emitter 에 이벤트 전송 */
    public <T> void send(String key, String eventName, T data) {
        List<SseEmitter> list = emitters.getOrDefault(key, Collections.emptyList());
        log.debug("📡 SSE Broadcast 시작 → key={}, emitters={}", key, list.size());
        for (SseEmitter emitter : list) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
                log.debug("✅ SSE send 성공 → key={}, emitter={}", key, emitter);
            } catch (IOException | IllegalStateException ex) {
                log.warn("⚠️ SSE send 실패, 제거 → key={}, emitter={}", key, emitter, ex);
                remove(key, emitter);
            }
        }
    }

    public <T> void send(String key, T data) {
        send(key, null, data);
    }

    private void remove(String key, SseEmitter emitter) {
        List<SseEmitter> list = emitters.get(key);
        if (list != null) list.remove(emitter);
        log.debug("➖ SSE emitter removed → key={}, remaining={}", key, list != null ? list.size() : 0);
    }

    /**
     * 15초마다 heartbeat 전송
     */
    @Scheduled(fixedRate = 15000)
    public void heartbeat() {
        emitters.forEach((key, list) -> {
            for (SseEmitter emitter : new ArrayList<>(list)) {
                try {
                    emitter.send(SseEmitter.event().name("ping").data("keep-alive"));
                    log.debug("💓 SSE heartbeat sent → key={}, emitter={}", key, emitter);
                } catch (IOException | IllegalStateException e) {
                    log.debug("💀 heartbeat용 emitter 제거 → key={}, emitter={}", key, emitter);
                    emitter.complete();
                    remove(key, emitter);
                }
            }
        });
    }
}
