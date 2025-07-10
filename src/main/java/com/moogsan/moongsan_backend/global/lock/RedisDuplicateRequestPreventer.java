package com.moogsan.moongsan_backend.global.lock;

import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Qualifier;

@Component
@Profile("!test")   // test 프로필 제외: 운영·dev·stage 등에서만 활성
public class RedisDuplicateRequestPreventer extends DuplicateRequestPreventer {

    public RedisDuplicateRequestPreventer(@Qualifier("redisTemplate") RedisTemplate<String, String> template) {
        super(template);
    }

    @Override
    public void checkAndSet(String key, long ttl) {
        // 필요 없으면 비워두거나, redisTemplate 사용해 커스텀 로직 작성
    }
}
