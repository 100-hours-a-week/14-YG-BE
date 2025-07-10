package com.moogsan.moongsan_backend.adapters.sse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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
@Component
public class SseEmitterRepository {

    private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    /** 신규 연결 등록 */
    public SseEmitter add(String key) {
        SseEmitter emitter = new SseEmitter(0L);
        emitters
                .computeIfAbsent(key, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(emitter);

        emitter.onCompletion(() -> remove(key, emitter));
        emitter.onTimeout   (() -> { emitter.complete(); remove(key, emitter); });

        return emitter;
    }

    /** 대상 키에 연결된 모든 emitter 에 이벤트 전송 */
    public <T> void send(String key, String eventName, T data) {
        List<SseEmitter> list = emitters.getOrDefault(key, Collections.emptyList());
        for (SseEmitter emitter : list) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (Exception ex) {
                emitter.completeWithError(ex);
            }
        }
    }

    private void remove(String key, SseEmitter emitter) {
        List<SseEmitter> list = emitters.get(key);
        if (list != null) list.remove(emitter);
    }
}
