package com.moogsan.moongsan_backend.unit.user.service;

import com.moogsan.moongsan_backend.domain.user.dto.request.LoginRequest;
import com.moogsan.moongsan_backend.domain.user.dto.response.LoginResponse;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import com.moogsan.moongsan_backend.domain.user.exception.base.UserException;
import com.moogsan.moongsan_backend.domain.user.repository.TokenRepository;
import com.moogsan.moongsan_backend.domain.user.repository.UserRepository;
import com.moogsan.moongsan_backend.domain.user.service.LoginService;
import com.moogsan.moongsan_backend.global.security.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoginServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private TokenRepository refreshTokenRepository;
    @Mock private HttpServletResponse response;

    @InjectMocks
    private LoginService loginService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private User getMockUser() {
        return User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .nickname("tester")
                .name("홍길동")
                .imageKey(null)
                .type("USER")
                .build();
    }

    @Test
    @DisplayName("로그인 성공")
    void login_success() {
        // given
        LoginRequest request = new LoginRequest("test@example.com", "plainPassword");
        User user = getMockUser();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("plainPassword", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateAccessToken(any())).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(any())).thenReturn("refresh-token");
        when(jwtUtil.getAccessTokenExpireAt()).thenReturn(3600000L); // 1h
        when(jwtUtil.getRefreshTokenExpireMillis()).thenReturn(604800000L); // 7d

        // when
        LoginResponse result = loginService.login(request, response);

        // then
        assertThat(result.getNickname()).isEqualTo("tester");
        assertThat(result.getName()).isEqualTo("홍길동");
        assertThat(result.getType()).isEqualTo("USER");
    }

    @Test
    @DisplayName("이메일이 없으면 예외")
    void login_blank_email() {
        LoginRequest request = new LoginRequest(" ", "password");

        assertThatThrownBy(() -> loginService.login(request, response))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("이메일은 필수 입력 값입니다.");
    }

    @Test
    @DisplayName("이메일 형식이 틀리면 예외")
    void login_invalid_email() {
        LoginRequest request = new LoginRequest("invalid-email", "password");

        assertThatThrownBy(() -> loginService.login(request, response))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("올바른 이메일 형식이어야 합니다.");
    }

    @Test
    @DisplayName("비밀번호가 비어 있으면 예외")
    void login_blank_password() {
        LoginRequest request = new LoginRequest("test@example.com", "");

        assertThatThrownBy(() -> loginService.login(request, response))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("비밀번호는 필수 입력 값입니다.");
    }

    @Test
    @DisplayName("존재하지 않는 이메일이면 예외")
    void login_email_not_found() {
        LoginRequest request = new LoginRequest("test@example.com", "password");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loginService.login(request, response))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("이메일이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("비밀번호 불일치 시 예외")
    void login_password_mismatch() {
        LoginRequest request = new LoginRequest("test@example.com", "wrongPassword");
        User user = getMockUser();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> loginService.login(request, response))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("비밀번호가 일치하지 않습니다.");
    }
}
