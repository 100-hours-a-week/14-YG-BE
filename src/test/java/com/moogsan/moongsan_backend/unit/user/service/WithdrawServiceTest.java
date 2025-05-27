package com.moogsan.moongsan_backend.unit.user.service;

import com.moogsan.moongsan_backend.domain.groupbuy.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.domain.order.repository.OrderRepository;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import com.moogsan.moongsan_backend.domain.user.exception.base.UserException;
import com.moogsan.moongsan_backend.domain.user.repository.TokenRepository;
import com.moogsan.moongsan_backend.domain.user.repository.UserRepository;
import com.moogsan.moongsan_backend.domain.user.service.WithdrawService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class WithdrawServiceTest {

    @InjectMocks
    private WithdrawService withdrawService;

    @Mock private UserRepository userRepository;
    @Mock private TokenRepository tokenRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private GroupBuyRepository groupBuyRepository;

    private final Long userId = 1L;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = User.builder().id(userId).build();
    }

    @Test
    @DisplayName("정상 탈퇴")
    void withdraw_success() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(orderRepository.existsByUserIdAndStatusNotIn(eq(userId), anyList())).thenReturn(false);
        when(groupBuyRepository.existsGroupBuyByUserIdAndPostStatusNot(userId, "ENDED")).thenReturn(false);

        withdrawService.withdraw(userId, response);

        verify(tokenRepository).deleteByUserId(userId);
        verify(userRepository).deleteById(userId);

        assertThat(response.getHeaders("Set-Cookie"))
                .anyMatch(cookie -> cookie.contains("AccessToken=;"))
                .anyMatch(cookie -> cookie.contains("RefreshToken=;"));
    }

    @Test
    @DisplayName("존재하지 않는 사용자")
    void withdraw_userNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> withdrawService.withdraw(userId, new MockHttpServletResponse()))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("존재하지 않는 사용자입니다.");
    }

    @Test
    @DisplayName("진행 중 공구 또는 주문이 있는 경우 탈퇴 실패")
    void withdraw_activeGroupOrOrder() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(orderRepository.existsByUserIdAndStatusNotIn(userId, List.of("CANCELED", "CONFIRMED"))).thenReturn(true);
        when(groupBuyRepository.existsGroupBuyByUserIdAndPostStatusNot(userId, "ENDED")).thenReturn(false);

        assertThatThrownBy(() -> withdrawService.withdraw(userId, new MockHttpServletResponse()))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("진행 중인 공구 또는 주문이 있습니다.");
    }
}