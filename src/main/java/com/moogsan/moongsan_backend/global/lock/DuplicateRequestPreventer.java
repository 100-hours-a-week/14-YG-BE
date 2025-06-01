package com.moogsan.moongsan_backend.global.lock;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public abstract class DuplicateRequestPreventer {

    private final RedisTemplate<String, String> redisTemplate;
    private static final Logger log = LoggerFactory.getLogger(DuplicateRequestPreventer.class);

    /**
     * 주어진 key가 없으면 등록하고 true, 이미 존재하면 false
     */
    public boolean tryAcquireLock(String key, long ttlSeconds) {
        try {
            // 중복 요청 방지를 위해 key를 TTL과 함께 삽입 (이미 있으면 false)
            Boolean success = redisTemplate.opsForValue()
                    .setIfAbsent(key, "LOCK", Duration.ofSeconds(ttlSeconds));

            return Boolean.TRUE.equals(success);

        } catch (RedisConnectionFailureException ex) {
            // Redis 서버 접속 실패(네트워크 문제 등)
            log.error("[DuplicateRequestPreventer] Redis 연결 실패 (key: {}, ttl: {}초): {}", key, ttlSeconds, ex.toString(), ex);
            return false;

        } catch (DataAccessException ex) {
            // Spring Data 또는 RedisTemplate 내부에서 던져지는 예외(ex: 인증 실패, 클라이언트 구성 오류 등)
            log.error("[DuplicateRequestPreventer] Redis 데이터 접근 오류 (key: {}, ttl: {}초): {}", key, ttlSeconds, ex.toString(), ex);
            return false;

        } catch (Exception ex) {
            // 그 외 예상치 못한 예외
            log.error("[DuplicateRequestPreventer] 알 수 없는 오류 발생 (key: {}, ttl: {}초): {}", key, ttlSeconds, ex.toString(), ex);
            return false;
        }
    }


    /**
     * 테스트용 삭제 메서드 (선택)
     */
    public void releaseLock(String key) {
        redisTemplate.delete(key);
    }

    public abstract void checkAndSet(String key, long ttl);
}
