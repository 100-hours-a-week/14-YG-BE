package com.moogsan.moongsan_backend.unit.user.controller;

import com.moogsan.moongsan_backend.domain.user.controller.OAuthController;
import com.moogsan.moongsan_backend.domain.user.dto.response.LoginResponse;
import com.moogsan.moongsan_backend.domain.user.service.KakaoOAuthService;
import com.moogsan.moongsan_backend.domain.WrapperResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OAuthControllerTest {

    @Test
    void kakaoLoginComplete_success() {
        // given
        String code = "test-code";
        String redirectUri = "https://dev.moongsan.com/kakao/callback";
        LoginResponse loginResponse = LoginResponse.builder()
            .nickname("nickname")
            .name("name")
            .imageUrl("test.png")
            .type("USER")
            .build();

        KakaoOAuthService kakaoOAuthService = Mockito.mock(KakaoOAuthService.class);
        HttpServletResponse httpResponse = Mockito.mock(HttpServletResponse.class);
        OAuthController controller = new OAuthController(kakaoOAuthService);

        Mockito.when(kakaoOAuthService.kakaoLogin(code, redirectUri, httpResponse))
                .thenReturn(loginResponse);

        // when
        ResponseEntity<WrapperResponse<?>> response = controller.kakaoLoginComplete(redirectUri, code, httpResponse);

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("로그인에 성공했습니다.", response.getBody().getMessage());

        LoginResponse responseData = (LoginResponse) response.getBody().getData();
        assertEquals("nickname", responseData.getNickname());
        assertEquals("name", responseData.getName());
        assertEquals("test.png", responseData.getImageUrl());
        assertEquals("USER", responseData.getType());
    }
}
