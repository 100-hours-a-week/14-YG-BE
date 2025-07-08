package com.moogsan.moongsan_backend.support.fake;

import com.moogsan.moongsan_backend.global.lock.DuplicateRequestPreventer;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 테스트 전용 DuplicateRequestPreventer.
 * Redis 대신 In-Memory Map에 TTL을 기록한다.
 */
@Component
@Profile("test")
public class InMemoryDuplicateRequestPreventer extends DuplicateRequestPreventer {

    private final ConcurrentMap<String, Long> store = new ConcurrentHashMap<>();

    public InMemoryDuplicateRequestPreventer() {
        super(null);               // RedisTemplate 필요 없음
    }

    /** 간단한 TTL 만료 로직 */
    @Override
    public boolean tryAcquireLock(String key, long ttlSeconds) {
        long now = System.currentTimeMillis() / 1000;
        store.entrySet().removeIf(e -> e.getValue() < now);      // 만료키 제거
        return store.putIfAbsent(key, now + ttlSeconds) == null; // true: 신규
    }

    @Override
    public void checkAndSet(String key, long ttlSeconds) {
        if (!tryAcquireLock(key, ttlSeconds)) {
            throw new IllegalStateException("DUPLICATE");        // 실제 예외로 교체
        }
    }
}
