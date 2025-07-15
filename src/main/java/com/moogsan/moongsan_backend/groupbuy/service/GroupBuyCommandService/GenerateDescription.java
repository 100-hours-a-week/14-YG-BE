package com.moogsan.moongsan_backend.groupbuy.service.GroupBuyCommandService;

import com.moogsan.moongsan_backend.groupbuy.client.AiClient;
import com.moogsan.moongsan_backend.groupbuy.dto.command.response.DescriptionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@Transactional
@RequiredArgsConstructor
public class GenerateDescription {
    private final AiClient aiClient;

    /// 공구 게시글 상세 설명 생성
    public Mono<DescriptionDto> generateDescription(String url, String sessionId) {
        return aiClient.generateDescription(url, sessionId);
    }
}
