package com.moogsan.moongsan_backend.global.infrastructure.kafka.publisher;

import com.moogsan.moongsan_backend.global.infrastructure.sse.SseEmitterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class GroupBuyRealtime {

    private final SseEmitterService emitterRepository;

    public SseEmitter subscribe(Long groupBuyId) {
        return emitterRepository.add(groupBuyId.toString());
    }
}
