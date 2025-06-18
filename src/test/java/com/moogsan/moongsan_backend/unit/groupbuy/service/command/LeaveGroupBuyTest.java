package com.moogsan.moongsan_backend.unit.groupbuy.service.command;

import com.moogsan.moongsan_backend.domain.chatting.Facade.command.ChattingCommandFacade;
import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyInvalidStateException;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyNotFoundException;
import com.moogsan.moongsan_backend.domain.groupbuy.policy.DueSoonPolicy;
import com.moogsan.moongsan_backend.domain.groupbuy.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyCommandService.CreateGroupBuy;
import com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyCommandService.LeaveGroupBuy;
import com.moogsan.moongsan_backend.domain.order.entity.Order;
import com.moogsan.moongsan_backend.domain.order.exception.specific.OrderNotFoundException;
import com.moogsan.moongsan_backend.domain.order.repository.OrderRepository;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static com.moogsan.moongsan_backend.domain.groupbuy.message.ResponseMessage.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LeaveGroupBuyTest {
    @Mock
    private GroupBuyRepository groupBuyRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private DueSoonPolicy dueSoonPolicy;
    @Mock private ChattingCommandFacade chattingCommandFacade;

    private LeaveGroupBuy leaveGroupBuy;
    private User participant;
    private GroupBuy before;
    private Order order;
    private Clock fixedClock;
    private LocalDateTime now;

    @BeforeEach
    void setup() {
        participant = User.builder().id(1L).build();
        before = mock(GroupBuy.class);
        order = mock(Order.class);

        fixedClock = Clock.fixed(
                Instant.parse("2025-06-11T13:00:00Z"),
                ZoneId.of("Asia/Seoul")
        );

        now = LocalDateTime.now(fixedClock);

        leaveGroupBuy = new LeaveGroupBuy(
                groupBuyRepository,
                orderRepository,
                dueSoonPolicy,
                chattingCommandFacade,
                fixedClock
        );
    }

    @Test
    @DisplayName("공구 참여 취소 성공")
    void leaveGroupBuy_success() {
        when(groupBuyRepository.findById(1L))
                .thenReturn(Optional.of(before));
        when(before.getPostStatus()).thenReturn("OPEN");
        when(before.getDueDate())
                .thenReturn(now.plusDays(1));
        when(before.getId()).thenReturn(20L);
        when(orderRepository.findByUserIdAndGroupBuyIdAndStatusNotIn(1L, 20L,
                List.of("CANCELED", "REFUNDED")))
                .thenReturn(Optional.of(order));

        leaveGroupBuy.leaveGroupBuy(participant, 1L);

        verify(groupBuyRepository, times(1)).findById(1L);
        verify(orderRepository, times(1))
                .findByUserIdAndGroupBuyIdAndStatusNotIn(1L, 20L, List.of("CANCELED", "REFUNDED"));
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("존재하지 않는 공구글 - 404 예외")
    void leaveGroupBuy_groupBuy_notFound() {
        when(groupBuyRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> leaveGroupBuy.leaveGroupBuy(participant, 1L))
                .isInstanceOf(GroupBuyNotFoundException.class)
                .hasMessageContaining(NOT_EXIST);

        verify(groupBuyRepository, times(1)).findById(1L);
        verify(orderRepository, never())
                .findByUserIdAndGroupBuyIdAndStatusNotIn(1L, 20L, List.of("CANCELED", "REFUNDED"));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("공구글 status가 OPEN이 아님 - 409 예외")
    void leaveGroupBuy_postStatus_not_open() {
        when(groupBuyRepository.findById(1L))
                .thenReturn(Optional.of(before));
        when(before.getPostStatus())
                .thenReturn("ENDED");

        assertThatThrownBy(() -> leaveGroupBuy.leaveGroupBuy(participant, 1L))
                .isInstanceOf(GroupBuyInvalidStateException.class)
                .hasMessageContaining(NOT_OPEN);

        verify(groupBuyRepository, times(1)).findById(1L);
        verify(orderRepository, never())
                .findByUserIdAndGroupBuyIdAndStatusNotIn(1L, 20L, List.of("CANCELED", "REFUNDED"));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("dueDate가 현재보다 과거 - 409 예외")
    void leaveGroupBuy_dueDate_past() {
        when(groupBuyRepository.findById(1L))
                .thenReturn(Optional.of(before));
        when(before.getPostStatus()).thenReturn("OPEN");
        when(before.getDueDate())
                .thenReturn(now.minusDays(1));

        assertThatThrownBy(() -> leaveGroupBuy.leaveGroupBuy(participant, 1L))
                .isInstanceOf(GroupBuyInvalidStateException.class)
                .hasMessageContaining(NOT_OPEN);

        verify(groupBuyRepository, times(1)).findById(1L);
        verify(orderRepository, never())
                .findByUserIdAndGroupBuyIdAndStatusNotIn(1L, 20L, List.of("CANCELED", "REFUNDED"));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("존재하지 않는 주문 - 404 예외")
    void leaveGroupBuy_order_notFound() {
        when(groupBuyRepository.findById(1L))
                .thenReturn(Optional.of(before));
        when(before.getPostStatus()).thenReturn("OPEN");
        when(before.getDueDate())
                .thenReturn(LocalDateTime.now().plusDays(1));
        when(before.getId()).thenReturn(20L);
        when(orderRepository.findByUserIdAndGroupBuyIdAndStatusNotIn(1L, 20L,
                List.of("CANCELED", "REFUNDED")))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> leaveGroupBuy.leaveGroupBuy(participant, 1L))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining(NOT_EXIST_ORDER);

        verify(groupBuyRepository, times(1)).findById(1L);
        verify(orderRepository, times(1))
                .findByUserIdAndGroupBuyIdAndStatusNotIn(1L, 20L, List.of("CANCELED", "REFUNDED"));
        verify(orderRepository, never()).save(any(Order.class));
    }
}
