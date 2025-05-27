package com.moogsan.moongsan_backend.unit.user.service;

import com.moogsan.moongsan_backend.domain.user.dto.request.SignUpRequest;
import com.moogsan.moongsan_backend.domain.user.dto.response.LoginResponse;
import com.moogsan.moongsan_backend.domain.user.exception.base.UserException;
import com.moogsan.moongsan_backend.domain.user.repository.TokenRepository;
import com.moogsan.moongsan_backend.domain.user.repository.UserRepository;
import com.moogsan.moongsan_backend.domain.user.service.SignUpService;
import com.moogsan.moongsan_backend.global.security.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class SignUpServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private TokenRepository tokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private HttpServletResponse response;

    @InjectMocks
    private SignUpService signUpService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("회원가입 성공 테스트")
    void signUp_success() {
        // given
        SignUpRequest request = SignUpRequest.builder()
                .email("test@example.com")
                .password("Abcdef1!")
                .nickname("tester")
                .name("홍길동")
                .phoneNumber("01012345678")
                .accountBank("신한은행")
                .accountNumber("110123456789")
                .imageUrl(null)
                .build();

        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.existsByNickname(any())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(any())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPW");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(jwtUtil.generateAccessToken(any())).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(any())).thenReturn("refresh-token");
        when(jwtUtil.getAccessTokenExpireAt()).thenReturn(3600000L);
        when(jwtUtil.getRefreshTokenExpireMillis()).thenReturn(604800000L); // 7일

        // when
        LoginResponse result = signUpService.signUp(request, response);

        // then
        assertThat(result.getNickname()).isEqualTo("tester");
        assertThat(result.getName()).isEqualTo("홍길동");
        assertThat(result.getType()).isEqualTo("USER");
    }

    @Test
    @DisplayName("중복된 이메일일 경우 예외 발생")
    void signUp_duplicateEmail() {
        // given
        SignUpRequest request = SignUpRequest.builder()
                .email("test@example.com")
                .password("Abcdef1!")
                .nickname("tester")
                .name("홍길동")
                .phoneNumber("01012345678")
                .accountBank("신한은행")
                .accountNumber("110123456789")
                .imageUrl("profile.png")
                .build();

        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // then
        assertThatThrownBy(() -> signUpService.signUp(request, response))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("이미 등록된 이메일입니다.");
    }

    @Test
    @DisplayName("잘못된 이메일 형식이면 예외 발생")
    void signUp_invalidEmailFormat() {
        SignUpRequest request = SignUpRequest.builder()
                .email("invalid-email")
                .password("Abcdef1!")
                .nickname("tester")
                .name("홍길동")
                .phoneNumber("01012345678")
                .accountBank("신한은행")
                .accountNumber("110123456789")
                .imageUrl("profile.png")
                .build();

        assertThatThrownBy(() -> signUpService.signUp(request, response))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("이메일의 형식이 올바르지 않습니다.");
    }
}