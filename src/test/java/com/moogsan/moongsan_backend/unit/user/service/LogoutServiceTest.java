package com.moogsan.moongsan_backend.unit.user.service;

import com.moogsan.moongsan_backend.domain.user.entity.User;
import com.moogsan.moongsan_backend.domain.user.exception.base.UserException;
import com.moogsan.moongsan_backend.domain.user.repository.TokenRepository;
import com.moogsan.moongsan_backend.domain.user.repository.UserRepository;
import com.moogsan.moongsan_backend.domain.user.service.LogoutService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class LogoutServiceTest {

    @InjectMocks
    private LogoutService logoutService;

    @Mock private UserRepository userRepository;
    @Mock private TokenRepository refreshTokenRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logout_success() {
        // given
        Long userId = 1L;
        User user = User.builder().id(userId).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        logoutService.logout(userId, response);

        // then
        verify(refreshTokenRepository, times(1)).deleteByUserId(userId);
        assertThat(user.getLogoutAt()).isNotNull();
        assertThat(response.getCookies()).hasSize(2);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 예외")
    void logout_userNotFound() {
        // given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> logoutService.logout(userId, new MockHttpServletResponse()))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("존재하지 않는 사용자입니다.");
    }

    @Test
    @DisplayName("예외 발생 시 INTERNAL_SERVER_ERROR")
    void logout_unexpectedException() {
        // given
        Long userId = 1L;
        User user = User.builder().id(userId).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        doThrow(new RuntimeException("DB 문제")).when(refreshTokenRepository).deleteByUserId(userId);

        // when & then
        assertThatThrownBy(() -> logoutService.logout(userId, new MockHttpServletResponse()))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("내부 서버 오류 발생");
    }
}