package com.moogsan.moongsan_backend.unit.user.service;

import com.moogsan.moongsan_backend.domain.user.entity.Token;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import com.moogsan.moongsan_backend.domain.user.exception.base.UserException;
import com.moogsan.moongsan_backend.domain.user.repository.TokenRepository;
import com.moogsan.moongsan_backend.domain.user.repository.UserRepository;
import com.moogsan.moongsan_backend.domain.user.service.TokenRefreshService;
import com.moogsan.moongsan_backend.global.security.jwt.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class TokenRefreshServiceTest {

    @InjectMocks
    private TokenRefreshService tokenRefreshService;

    @Mock private JwtUtil jwtUtil;
    @Mock private TokenRepository tokenRepository;
    @Mock private UserRepository userRepository;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private Cookie[] createRefreshCookie(String value) {
        return new Cookie[]{ new Cookie("RefreshToken", value) };
    }

    private User getMockUser(Long userId) {
        return User.builder()
                .id(userId)
                .email("test@example.com")
                .build();
    }

    @Test
    @DisplayName("정상적인 Refresh Token → Access Token 재발급 성공")
    void refreshAccessToken_success() {
        String refreshToken = "valid-token";
        Long userId = 1L;
        User user = getMockUser(userId);
        Token token = Token.builder().userId(user.getId()).token(refreshToken).build();

        when(request.getCookies()).thenReturn(createRefreshCookie(refreshToken));
        when(jwtUtil.validateToken(refreshToken)).thenReturn(true);
        when(jwtUtil.getUserIdFromToken(refreshToken)).thenReturn(userId);
        when(tokenRepository.findByUserId(userId)).thenReturn(Optional.of(token));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(jwtUtil.generateAccessToken(user)).thenReturn("new-access-token");
        when(jwtUtil.getAccessTokenExpireAt()).thenReturn(3600000L);

        assertThatCode(() -> tokenRefreshService.refreshAccessToken(request, response))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("쿠키에 RefreshToken이 없을 경우 예외 발생")
    void refreshAccessToken_cookie_missing() {
        when(request.getCookies()).thenReturn(null);

        assertThatThrownBy(() -> tokenRefreshService.refreshAccessToken(request, response))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("Refresh Token이 유효하지 않습니다.");
    }

    @Test
    @DisplayName("RefreshToken 유효성 검사 실패")
    void refreshAccessToken_invalid_token() {
        String token = "invalid";
        when(request.getCookies()).thenReturn(createRefreshCookie(token));
        when(jwtUtil.validateToken(token)).thenReturn(false);

        assertThatThrownBy(() -> tokenRefreshService.refreshAccessToken(request, response))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("Refresh Token이 유효하지 않습니다.");
    }

    @Test
    @DisplayName("DB에 저장된 Refresh Token이 없을 경우")
    void refreshAccessToken_stored_token_not_found() {
        String token = "valid-token";
        Long userId = 1L;

        when(request.getCookies()).thenReturn(createRefreshCookie(token));
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.getUserIdFromToken(token)).thenReturn(userId);
        when(tokenRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tokenRefreshService.refreshAccessToken(request, response))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("저장된 Refresh Token이 없습니다.");
    }

    @Test
    @DisplayName("저장된 Refresh Token과 일치하지 않을 경우")
    void refreshAccessToken_token_mismatch() {
        String inputToken = "token-A";
        String storedToken = "token-B";
        Long userId = 1L;
        User user = getMockUser(userId);

        when(request.getCookies()).thenReturn(createRefreshCookie(inputToken));
        when(jwtUtil.validateToken(inputToken)).thenReturn(true);
        when(jwtUtil.getUserIdFromToken(inputToken)).thenReturn(userId);
        when(tokenRepository.findByUserId(userId))
                .thenReturn(Optional.of(Token.builder().userId(user.getId()).token(storedToken).build()));

        assertThatThrownBy(() -> tokenRefreshService.refreshAccessToken(request, response))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("Refresh Token이 일치하지 않습니다.");
    }

    @Test
    @DisplayName("유저 정보를 찾을 수 없는 경우")
    void refreshAccessToken_user_not_found() {
        String token = "valid-token";
        Long userId = 1L;
        User user = getMockUser(userId);
        Token tokenEntity = Token.builder().userId(user.getId()).token(token).build();

        when(request.getCookies()).thenReturn(createRefreshCookie(token));
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.getUserIdFromToken(token)).thenReturn(userId);
        when(tokenRepository.findByUserId(userId)).thenReturn(Optional.of(tokenEntity));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tokenRefreshService.refreshAccessToken(request, response))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("유저 정보를 찾을 수 없습니다.");
    }
}