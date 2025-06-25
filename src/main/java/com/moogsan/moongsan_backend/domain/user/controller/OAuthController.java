package com.moogsan.moongsan_backend.domain.user.controller;

import com.moogsan.moongsan_backend.domain.WrapperResponse;
import com.moogsan.moongsan_backend.domain.user.dto.response.LoginResponse;
import com.moogsan.moongsan_backend.domain.user.service.KakaoOAuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class OAuthController {
    private final KakaoOAuthService kakaoOAuthService;

    @Value("${app.oauth.kakao-complete-redirect}")
    private String kakaoCompleteRedirectUrl;

    @GetMapping("/kakao/callback")
    public void kakaoCallback(@RequestParam("code") String code, HttpServletResponse response) {
        log.debug("카카오로부터 사용자 코드를 받아옴: {}", code);
    }

    @GetMapping("/api/oauth/kakao/callback/complete")
    public ResponseEntity<WrapperResponse<?>> kakaoLoginComplete(
            @RequestParam("code") String code,
            HttpServletResponse response) {
        log.debug("카카오 로그인 성공: {}", code);

        LoginResponse loginResponse = kakaoOAuthService.kakaoLogin(code, response);
        log.debug("서비스 로그인 성공: {}", code);

        return ResponseEntity.ok(
                WrapperResponse.builder()
                        .message("로그인에 성공했습니다.")
                        .data(loginResponse)
                        .build()
        );
    }
}