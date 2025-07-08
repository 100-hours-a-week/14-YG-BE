package com.moogsan.moongsan_backend.domain.user.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import com.moogsan.moongsan_backend.domain.user.dto.response.KakaoTokenResponse;
import com.moogsan.moongsan_backend.domain.user.dto.response.KakaoUserInfoResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoOAuthClient {

    // HTTP 요청을 위한 RestTemplate 인스턴스 생성
    private final RestTemplate restTemplate = new RestTemplate();

    // application.yml 또는 properties에서 Kakao REST API 키 주입
    @Value("${oauth.kakao.client-id}")
    private String clientId;

    // 인가 코드를 이용해 Kakao로부터 Access Token을 요청하는 메서드
    public KakaoTokenResponse requestAccessToken(String code, String redirectUri) {
        String tokenUri = "https://kauth.kakao.com/oauth/token";

        // HTTP 요청 헤더 설정 (Content-Type: application/x-www-form-urlencoded)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 요청 본문 설정
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("redirect_uri", redirectUri);
        body.add("code", code);

        // 헤더와 바디를 포함한 HTTP 요청 객체 생성
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        // POST 요청 전송 및 응답 수신
        ResponseEntity<KakaoTokenResponse> response = restTemplate.exchange(
                tokenUri,
                HttpMethod.POST,
                request,
                KakaoTokenResponse.class
        );

        // 응답 본문 반환 (Access Token 포함)
        return response.getBody();
    }

    // Access Token을 사용해 Kakao 사용자 정보를 요청하는 메서드
    public KakaoUserInfoResponse requestUserInfo(String accessToken) {
        String userInfoUri = "https://kapi.kakao.com/v2/user/me";

        // HTTP 요청 헤더 설정 (Authorization: Bearer {accessToken})
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        // 헤더만 포함한 HTTP 요청 객체 생성
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // GET 요청 전송 및 응답 수신
        ResponseEntity<KakaoUserInfoResponse> response = restTemplate.exchange(
                userInfoUri,
                HttpMethod.GET,
                request,
                KakaoUserInfoResponse.class
        );

        // 응답 본문 반환 (사용자 정보 포함)
        return response.getBody();
    }
}