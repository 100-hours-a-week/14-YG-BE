package com.moogsan.moongsan_backend.domain.user.controller;

import com.moogsan.moongsan_backend.domain.WrapperResponse;
import com.moogsan.moongsan_backend.domain.user.dto.response.LoginResponse;
import com.moogsan.moongsan_backend.domain.user.service.KakaoOAuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class OAuthController {
    private final KakaoOAuthService kakaoOAuthService;

    @GetMapping("/kakao/callback")
    public void kakaoCallback(@RequestParam("code") String code, HttpServletResponse response) {
        log.debug("카카오로부터 사용자 코드를 받아옴: {}", code);
    }

    @GetMapping("/api/oauth/kakao/callback/complete")
    public ResponseEntity<WrapperResponse<?>> kakaoLoginComplete(
            @RequestParam("redirectUri") String redirectUri,
            @RequestParam("code") String code,
            HttpServletResponse response) {

        log.debug("카카오 로그인 redirectUri: {}", redirectUri);
        log.debug("카카오 로그인 성공: {}", code);

        Object result = kakaoOAuthService.kakaoLogin(code, redirectUri, response);

        log.debug("서비스 로그인 성공: {}", code);

        if (result instanceof LoginResponse loginResponse) {
            return ResponseEntity.ok(
                    WrapperResponse.builder()
                            .message("로그인에 성공했습니다.")
                            .data(loginResponse)
                            .build()
            );
        } else {
            return ResponseEntity.ok(
                    WrapperResponse.builder()
                            .message("회원가입이 필요합니다.")
                            .data(result)
                            .build()
            );
        }
    }
}