package com.moogsan.moongsan_backend.domain.chatting.participant.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class MessageSequenceGenerator {

    private final RedisTemplate<String, String> redisTemplate;

    public MessageSequenceGenerator(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Long getNextMessageSeq(Long chatRoomId) {
        String key = "chat:seq:" + chatRoomId;
        return redisTemplate.opsForValue().increment(key);
    }
}
