package com.moogsan.moongsan_backend.domain.user.controller;

import com.moogsan.moongsan_backend.domain.WrapperResponse;
import com.moogsan.moongsan_backend.domain.user.dto.response.LoginResponse;
import com.moogsan.moongsan_backend.domain.user.service.KakaoOAuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/oauth")
@RequiredArgsConstructor
public class OAuthController {
    private final KakaoOAuthService kakaoOAuthService;

    @GetMapping("/kakao/callback")
    public ResponseEntity<WrapperResponse<?>> kakaoCallback(@RequestParam("code") String code, HttpServletResponse response) {
        LoginResponse loginResponse = kakaoOAuthService.kakaoLogin(code, response);

        return ResponseEntity.ok(
            WrapperResponse.builder()
                .message("로그인에 성공했습니다.")
                .data(loginResponse)
                .build()
        );
    }
}
