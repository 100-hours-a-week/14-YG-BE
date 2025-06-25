package com.moogsan.moongsan_backend.unit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moogsan.moongsan_backend.domain.user.dto.response.LoginResponse;
import com.moogsan.moongsan_backend.domain.user.service.KakaoOAuthService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@Disabled
//@SpringBootTest
//@AutoConfigureMockMvc
//class OAuthControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private KakaoOAuthService kakaoOAuthService;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @TestConfiguration
//    static class TestConfig {
//        @Bean
//        public KakaoOAuthService kakaoOAuthService() {
//            return Mockito.mock(KakaoOAuthService.class);
//        }
//    }
//
//    @Test
//    @DisplayName("카카오 OAuth 콜백 성공 테스트")
//    void kakaoCallback_success() throws Exception {
//        // given
//        LoginResponse fakeResponse = LoginResponse.builder()
//                .nickname("nickname")
//                .name("박건")
//                .imageUrl(null)
//                .type("USER")
//                .build();
//
//        Mockito.when(kakaoOAuthService.kakaoLogin(eq("auth-code"), any(HttpServletResponse.class)))
//                .thenReturn(fakeResponse);
//
//        // when & then
//        mockMvc.perform(get("/api/oauth/kakao/callback")
//                        .param("code", "auth-code"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("로그인에 성공했습니다."))
//                .andExpect(jsonPath("$.data.nickname").value("nickname"))
//                .andExpect(jsonPath("$.data.name").value("박건"))
//                .andExpect(jsonPath("$.data.imageUrl").doesNotExist())
//                .andExpect(jsonPath("$.data.type").value("USER"));
//    }
//
//    @Test
//    @DisplayName("code 파라미터 없이 요청 시 400 에러 발생")
//    void kakaoCallback_missingCode_shouldReturnBadRequest() throws Exception {
//        mockMvc.perform(get("/api/oauth/kakao/callback"))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    @DisplayName("카카오 로그인 중 예외 발생 시 500 에러 반환")
//    void kakaoCallback_serviceThrowsException_shouldReturnInternalServerError() throws Exception {
//        Mockito.when(kakaoOAuthService.kakaoLogin(eq("auth-code"), any(HttpServletResponse.class)))
//                .thenThrow(new RuntimeException("OAuth 요청 중 오류 발생"));
//
//        mockMvc.perform(get("/api/oauth/kakao/callback")
//                        .param("code", "auth-code"))
//                .andExpect(status().isInternalServerError())
//                .andExpect(jsonPath("$.message").value("OAuth 요청 중 오류 발생"));
//    }
//}
