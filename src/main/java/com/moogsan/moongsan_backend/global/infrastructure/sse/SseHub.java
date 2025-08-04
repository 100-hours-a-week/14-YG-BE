package com.moogsan.moongsan_backend.global.infrastructure.sse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class SseHub {
    private static final long TIMEOUT_MS = 2 * 60 * 60 * 1000L;

    private final ConcurrentMap<String, EmitterCtx> connections = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Set<String>> topicToConns = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, Set<String>> userToConns = new ConcurrentHashMap<>();

    private final SseSender sender;

    public SseEmitter open(
            Long userId,
            @Nullable Set<String> initialTopics
    ) {
        String connId = UUID.randomUUID().toString();
        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);
        Set<String> topics = ConcurrentHashMap.newKeySet();
        topics.add("user:" + userId);
        if (initialTopics != null) {
            topics.addAll(initialTopics);
        }

        EmitterCtx ctx = new EmitterCtx(connId, userId, emitter, topics);
        connections.put(connId, ctx);
        userToConns.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(connId);
        topics.forEach(t -> topicToConns.computeIfAbsent(t, k -> ConcurrentHashMap.newKeySet()).add(connId));

        emitter.onCompletion(() -> cleanup(connId, ctx));
        emitter.onTimeout(() -> cleanup(connId, ctx));
        emitter.onError(ex -> cleanup(connId, ctx));

        sender.sendAsync(emitter, "CONNECT", Map.of("connId", connId));
        return emitter;
    }

    public void updateSubscriptions(
            Long userId,
            @Nullable String connectionId,
            @Nullable Set<String> add,
            @Nullable Set<String> remove
    ) {
        for (String cid : resolveTargetConnIds(userId, connectionId)) {
            EmitterCtx ctx = connections.get(cid);
            if (ctx == null) continue;
            if (add != null) {
                add.forEach(t -> {
                    ctx.topics().add(t);
                    topicToConns.computeIfAbsent(t, k -> ConcurrentHashMap.newKeySet()).add(cid);
                });
            }
            if (remove != null) {
                remove.forEach(t -> {
                    ctx.topics().remove(t);
                    Set<String> set = topicToConns.get(t);
                    if (set != null) {
                        set.remove(cid);
                        if (set.isEmpty()) {
                            topicToConns.remove(t);
                        }
                    }
                });
            }
        }
    }

    public <T> void publish(String topic, String eventName, T envelopeOrData) {
        for (String cid : topicToConns.getOrDefault(topic, Set.of())) {
            EmitterCtx ctx = connections.get(cid);
            if (ctx != null) {
                sender.sendAsync(ctx.emitter(), eventName, envelopeOrData);
            }
        }
    }

    private Collection<String> resolveTargetConnIds(
            Long userId,
            @Nullable String connId
    ) {
        if (connId != null) {
            return List.of(connId);
        }
        return new ArrayList<>(userToConns.getOrDefault(userId, Set.of()));
    }

    private void cleanup(String connId, EmitterCtx ctx) {
        connections.remove(connId);
        ctx.topics().forEach(t -> {
            Set<String> set = topicToConns.get(t);
            if (set != null) {
                set.remove(connId);
                if (set.isEmpty()) {
                    topicToConns.remove(t);
                }
            }
        });
        Set<String> u = userToConns.get(ctx.userId());
        if (u != null) {
            u.remove(connId);
            if (u.isEmpty()) {
                userToConns.remove(ctx.userId());
            }
        }
    }

    /**
     * 15초마다 heartbeat 전송
     */
    @Scheduled(fixedRate = 15000)
    public void heartbeat() {
        connections.values().forEach(ctx -> sender.sendAsync(ctx.emitter(), "PING", "keep-alive"));
    }
}
