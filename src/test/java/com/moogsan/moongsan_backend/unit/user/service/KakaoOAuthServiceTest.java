package com.moogsan.moongsan_backend.unit.user.service;

import com.moogsan.moongsan_backend.domain.user.component.KakaoOAuthClient;
import com.moogsan.moongsan_backend.domain.user.dto.response.KakaoTokenResponse;
import com.moogsan.moongsan_backend.domain.user.dto.response.KakaoUserInfoResponse;
import com.moogsan.moongsan_backend.domain.user.dto.response.LoginResponse;
import com.moogsan.moongsan_backend.domain.user.entity.OAuth;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import com.moogsan.moongsan_backend.domain.user.exception.base.UserException;
import com.moogsan.moongsan_backend.domain.user.repository.OAuthRepository;
import com.moogsan.moongsan_backend.domain.user.repository.TokenRepository;
import com.moogsan.moongsan_backend.domain.user.repository.UserRepository;
import com.moogsan.moongsan_backend.domain.user.service.KakaoOAuthService;
import com.moogsan.moongsan_backend.global.security.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.moogsan.moongsan_backend.domain.user.exception.code.UserErrorCode.SIGNUP_REQUIRED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class KakaoOAuthServiceTest {

    private final String code = "auth-code";
    private final String redirectUri = "https://localhost/kakao/callback";

    private KakaoTokenResponse tokenResponse;
    private KakaoUserInfoResponse userInfoResponse;

    @BeforeEach
    void setUp() {
        tokenResponse = new KakaoTokenResponse();
        tokenResponse.setAccessToken("access-token");
        tokenResponse.setRefreshToken("refresh-token");
        tokenResponse.setExpiresIn(3600);
        tokenResponse.setTokenType("bearer");

        userInfoResponse = new KakaoUserInfoResponse();
        KakaoUserInfoResponse.KakaoAccount kakaoAccount = new KakaoUserInfoResponse.KakaoAccount();
        KakaoUserInfoResponse.Profile profile = new KakaoUserInfoResponse.Profile();

        kakaoAccount.setEmail("test@kakao.com");
        profile.setNickname("닉네임");
        profile.setProfileImageUrl("프로필");
        kakaoAccount.setProfile(profile);
        userInfoResponse.setKakaoAccount(kakaoAccount);
    }

    private final KakaoOAuthClient kakaoOAuthClient = mock(KakaoOAuthClient.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final OAuthRepository oAuthRepository = mock(OAuthRepository.class);
    private final JwtUtil jwtUtil = mock(JwtUtil.class);
    private final TokenRepository tokenRepository = mock(TokenRepository.class);
    private final HttpServletResponse response = mock(HttpServletResponse.class);

    private final KakaoOAuthService kakaoOAuthService = new KakaoOAuthService(
            kakaoOAuthClient,
            userRepository,
            oAuthRepository,
            jwtUtil,
            tokenRepository
    );

    @Test
    @DisplayName("카카오 로그인 - 성공 케이스")
    void kakaoLogin_success() {
        String email = "test@kakao.com";

        User user = User.builder()
                .email(email)
                .nickname("닉네임")
                .name("홍길동")
                .imageKey("프로필")
                .type("USER")
                .build();

        OAuth oAuth = OAuth.builder()
                .provider("kakao")
                .user(user)
                .build();

        String accessToken = "access.jwt.token";
        String refreshToken = "refresh.jwt.token";
        long accessTokenExpireAt = System.currentTimeMillis() + 3600_000L;

        when(kakaoOAuthClient.requestAccessToken(code, redirectUri)).thenReturn(tokenResponse);
        when(kakaoOAuthClient.requestUserInfo("access-token")).thenReturn(userInfoResponse);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(oAuthRepository.findByUserAndProvider(user, "kakao")).thenReturn(Optional.of(oAuth));
        when(jwtUtil.generateAccessToken(user)).thenReturn(accessToken);
        when(jwtUtil.generateRefreshToken(user)).thenReturn(refreshToken);
        when(jwtUtil.getAccessTokenExpireAt()).thenReturn(accessTokenExpireAt);

        LoginResponse result = kakaoOAuthService.kakaoLogin(code, redirectUri, response);

        assertThat(result.getNickname()).isEqualTo("닉네임");
        assertThat(result.getName()).isEqualTo("홍길동");
        assertThat(result.getImageUrl()).isEqualTo("프로필");
        assertThat(result.getType()).isEqualTo("USER");
    }

    @Test
    @DisplayName("카카오 로그인 - 회원가입 필요")
    void kakaoLogin_needSignup() {
        when(kakaoOAuthClient.requestAccessToken(code, redirectUri)).thenReturn(tokenResponse);
        when(kakaoOAuthClient.requestUserInfo("access-token")).thenReturn(userInfoResponse);
        when(userRepository.findByEmail("test@kakao.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> kakaoOAuthService.kakaoLogin(code, redirectUri, response))
                .isInstanceOf(UserException.class)
                .hasMessageContaining(SIGNUP_REQUIRED.getDefaultMessage());
    }

    @Test
    @DisplayName("카카오 로그인 - 카카오 서버 오류")
    void kakaoLogin_kakaoError() {
        when(kakaoOAuthClient.requestAccessToken(code, redirectUri))
                .thenThrow(new RuntimeException("카카오 서버 오류"));

        assertThatThrownBy(() -> kakaoOAuthService.kakaoLogin(code, redirectUri, response))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("OAuth 요청 중 오류 발생");
    }
}
