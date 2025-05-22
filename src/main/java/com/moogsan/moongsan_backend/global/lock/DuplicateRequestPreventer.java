package com.moogsan.moongsan_backend.global.lock;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class DuplicateRequestPreventer {

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 주어진 key가 없으면 등록하고 true, 이미 존재하면 false
     */
    public boolean tryAcquireLock(String key, long ttlSeconds) {
        // 중복 요청 방지를 위해 key를 TTL과 함께 삽입 (이미 있으면 false)
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, "LOCK", Duration.ofSeconds(ttlSeconds));

        return Boolean.TRUE.equals(success);
    }

    /**
     * 테스트용 삭제 메서드 (선택)
     */
    public void releaseLock(String key) {
        redisTemplate.delete(key);
    }
}
