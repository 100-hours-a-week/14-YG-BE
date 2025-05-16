// 1) AiClient.java
package com.moogsan.moongsan_backend.domain.groupbuy.client;

import com.moogsan.moongsan_backend.domain.WrapperResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.command.request.DescriptionGenerationRequest;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.command.response.ApiResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.command.response.DescriptionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiClient {

    private final WebClient webClient;

    @Value("${ai.service.base-url}")
    private String aiBaseUrl;

    /**
     * 외부 AI API 호출 → ApiResponse<DescriptionDto> 로 파싱 → DescriptionDto 추출
     */
    public Mono<DescriptionDto> generateDescription(String url, String sessionId) {
        log.info("[AiClient] aiBaseUrl = {}", aiBaseUrl);
        log.info("[AiClient] full endpoint = {}", aiBaseUrl + "/generation/description");

        DescriptionGenerationRequest req = DescriptionGenerationRequest.builder()
                .url(url)
                .build();

        return webClient.post()
                .uri(aiBaseUrl + "/generation/description")
                .cookie("Session", sessionId)
                .bodyValue(req)
                .retrieve()
                // 4xx 클라이언트 에러면 Bad Request로 매핑
                .onStatus(HttpStatusCode::is4xxClientError,
                        resp -> Mono.error(new IllegalArgumentException("유효하지 않은 URL 형식입니다.")))
                // 5xx 서버 에러면 Internal Server Error로 매핑
                .onStatus(HttpStatusCode::is5xxServerError,
                        resp -> Mono.error(new IllegalStateException("AI 서비스 호출 중 서버 오류가 발생했습니다.")))
                // 외부 AI의 공통 래퍼(ApiResponse<T>) 형태로 역직렬화
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<DescriptionDto>>() {})
                .flatMap(apiRes -> {
                    DescriptionDto dto = apiRes.getData();  // 이제 data 안에 payload만 담겨 있음
                    if (dto != null) {
                        return Mono.just(dto);
                    } else {
                        return Mono.error(new IllegalStateException("AI 서비스가 데이터를 반환하지 않았습니다."));
                    }
                });
    }
}
