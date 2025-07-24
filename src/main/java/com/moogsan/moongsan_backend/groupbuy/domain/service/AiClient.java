// 1) AiClient.java
package com.moogsan.moongsan_backend.groupbuy.domain.service;

import com.moogsan.moongsan_backend.domain.user.entity.User;
import com.moogsan.moongsan_backend.groupbuy.domain.dto.ChatBotRequest;
import com.moogsan.moongsan_backend.groupbuy.presentation.dto.command.request.ChatMessageRequest;
import com.moogsan.moongsan_backend.groupbuy.presentation.dto.command.request.DescriptionGenerationRequest;
import com.moogsan.moongsan_backend.groupbuy.presentation.dto.command.response.ApiResponse;
import com.moogsan.moongsan_backend.groupbuy.presentation.dto.command.response.DescriptionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

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
                .cookie("AccessToken", sessionId)
                .bodyValue(req)
                .retrieve()
                // 4xx 클라이언트 에러면 Bad Request로 매핑
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    if (response.statusCode() == HttpStatus.BAD_REQUEST) {
                        return Mono.error(new IllegalArgumentException("요청 형식이 잘못되었습니다. (400 Bad Request)"));
                    } else if (response.statusCode() == HttpStatus.UNAUTHORIZED) {
                        return Mono.error(new SecurityException("인증 정보가 없습니다. (401 Unauthorized)"));
                    } else {
                        return Mono.error(new RuntimeException("클라이언트 오류가 발생했습니다. 상태 코드: " + response.statusCode()));
                    }
                })
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

    /**  챗봇 스트리밍 **/
    public Flux<String> streamChat(User user, ChatMessageRequest req, String sessionId) {
        log.info("[AiClient] chat endpoint = {}/chat/stream", aiBaseUrl);

        ChatBotRequest chatBotRequest = ChatBotRequest.builder()
                .message(req.getMessage())
                .sessionId(req.getSessionId())
                .userId(user.getId())
                .userName(user.getName())
                .build();

        return webClient.post()
                .uri(aiBaseUrl + "/chat/stream")
                .cookie("AccessToken", sessionId)
                .accept(MediaType.TEXT_EVENT_STREAM)                 // SSE 구독
                .bodyValue(chatBotRequest)
                .retrieve()
                .bodyToFlux(String.class)                             // SSE 라인 단위 Flux
                .doOnError(e -> log.error("[AiClient] streamChat error", e))
                .onErrorResume(e -> Flux.just(
                        // 에러를 직접 JSON 문자열로 감싸 보낼 수도 있고
                        "{\"type\":\"error\",\"content\":\"챗봇 오류 발생: "
                                + e.getMessage() + "\",\"agent\":\"chat\",\"timestamp\":\""
                                + Instant.now().toString() + "\"}"
                ));
    }
}
