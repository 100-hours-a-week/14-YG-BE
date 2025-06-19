package com.moogsan.moongsan_backend.domain.user.controller;

import com.moogsan.moongsan_backend.domain.WrapperResponse;
import com.moogsan.moongsan_backend.domain.user.dto.response.LoginResponse;
import com.moogsan.moongsan_backend.domain.user.service.KakaoOAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/oauth")
@RequiredArgsConstructor
public class OAuthController {
    private final KakaoOAuthService kakaoOAuthService;

    @Value("${app.oauth.kakao-complete-redirect}")
    private String kakaoCompleteRedirectUrl;

    @GetMapping("/kakao/callback")
    public void kakaoCallback(@RequestParam("code") String code, HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.debug("Received Kakao OAuth callback with code: {}", code);
        request.getSession(true).setAttribute("kakaoCode", code);
        response.sendRedirect(kakaoCompleteRedirectUrl);
    }

    @GetMapping("/kakao/callback/response")
    public ResponseEntity<WrapperResponse<LoginResponse>> kakaoLoginResponse(HttpServletRequest request, HttpServletResponse response) {
        String code = (String) request.getSession().getAttribute("kakaoCode");
        log.debug("Fetching login response with code from session: {}", code);

        LoginResponse loginResponse = kakaoOAuthService.kakaoLogin(code, response);
        request.getSession().removeAttribute("kakaoCode");

        return ResponseEntity.ok(
                WrapperResponse.<LoginResponse>builder()
                        .message("로그인에 성공했습니다.")
                        .data(loginResponse)
                        .build()
        );
    }

    @GetMapping("/kakao/callback/complete")
    public String oauthComplete() {
        log.debug("OAuth login complete page accessed.");
        return "OAuth 로그인 처리가 완료되었습니다.";
    }
//    @GetMapping("/kakao/callback")
//    public ResponseEntity<WrapperResponse<?>> kakaoCallback(@RequestParam("code") String code, HttpServletResponse response) {
//        LoginResponse loginResponse = kakaoOAuthService.kakaoLogin(code, response);
//
//        return ResponseEntity.ok(
//            WrapperResponse.builder()
//                    .message("로그인에 성공했습니다.")
//                    .data(loginResponse)
//                    .build()
//        );
//    }
}
