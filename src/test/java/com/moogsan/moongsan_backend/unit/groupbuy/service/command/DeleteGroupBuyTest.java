package com.moogsan.moongsan_backend.unit.groupbuy.service.command;

import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyInvalidStateException;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyNotFoundException;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyNotHostException;
import com.moogsan.moongsan_backend.domain.groupbuy.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyCommandService.DeleteGroupBuy;
import com.moogsan.moongsan_backend.domain.image.mapper.ImageMapper;
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
import java.util.Optional;

import static com.moogsan.moongsan_backend.domain.groupbuy.message.ResponseMessage.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DeleteGroupBuyTest {

    @Mock
    private GroupBuyRepository groupBuyRepository;

    @Mock
    private OrderRepository orderRepository;

    private DeleteGroupBuy deleteGroupBuy;
    private User hostUser;
    private GroupBuy before;
    private Clock fixedClock;
    private LocalDateTime now;

    @BeforeEach
    void setup() {
        hostUser = User.builder().id(1L).build();
        before = mock(GroupBuy.class);

        fixedClock = Clock.fixed(
                Instant.parse("2025-06-11T13:00:00Z"),
                ZoneId.of("Asia/Seoul")
        );

        now = LocalDateTime.now(fixedClock);

        deleteGroupBuy = new DeleteGroupBuy(
                groupBuyRepository,
                orderRepository,
                fixedClock
        );
    }

    @Test
    @DisplayName("공구 삭제 성공")
    void deleteGroupBuy_success() {
        when(groupBuyRepository.findById(1L))
                .thenReturn(Optional.of(before));
        when(before.getPostStatus()).thenReturn("OPEN");
        when(before.getDueDate())
                .thenReturn(LocalDateTime.now().plusDays(1));
        when(orderRepository.countByGroupBuyIdAndStatusNot(1L, "CANCELED"))
                .thenReturn(0);
        when(before.getUser()).thenReturn(hostUser);

        deleteGroupBuy.deleteGroupBuy(hostUser, 1L);

        verify(groupBuyRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).countByGroupBuyIdAndStatusNot(1L, "CANCELED");
        verify(groupBuyRepository, times(1)).save(any(GroupBuy.class));
    }

    @Test
    @DisplayName("존재하지 않는 공구글 - 404 예외")
    void deleteGroupBuy_notFound() {
        when(groupBuyRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> deleteGroupBuy.deleteGroupBuy(hostUser, 1L))
                .isInstanceOf(GroupBuyNotFoundException.class)
                .hasMessageContaining(NOT_EXIST);

        verify(groupBuyRepository, times(1)).findById(1L);
        verify(orderRepository, never()).countByGroupBuyIdAndStatusNot(1L, "CANCELED");
        verify(groupBuyRepository, never()).save(any(GroupBuy.class));
    }

    @Test
    @DisplayName("공구글 status가 OPEN이 아님 - 409 예외")
    void deleteGroupBuy_postStatus_not_open() {
        when(groupBuyRepository.findById(1L))
                .thenReturn(Optional.of(before));
        when(before.getPostStatus()).thenReturn("ENDED");

        assertThatThrownBy(() -> deleteGroupBuy.deleteGroupBuy(hostUser, 1L))
                .isInstanceOf(GroupBuyInvalidStateException.class)
                .hasMessageContaining(NOT_OPEN);

        verify(groupBuyRepository, times(1)).findById(1L);
        verify(orderRepository, never()).countByGroupBuyIdAndStatusNot(1L, "CANCELED");
        verify(groupBuyRepository, never()).save(any(GroupBuy.class));
    }

    @Test
    @DisplayName("dueDate가 현재보다 과거 - 409 예외")
    void deleteGroupBuy_dueDate_past() {
        when(groupBuyRepository.findById(1L))
                .thenReturn(Optional.of(before));
        when(before.getPostStatus()).thenReturn("OPEN");
        when(before.getDueDate())
                .thenReturn(now.minusDays(1));

        assertThatThrownBy(() -> deleteGroupBuy.deleteGroupBuy(hostUser, 1L))
                .isInstanceOf(GroupBuyInvalidStateException.class)
                .hasMessageContaining(NOT_OPEN);

        verify(groupBuyRepository, times(1)).findById(1L);
        verify(orderRepository, never()).countByGroupBuyIdAndStatusNot(1L, "CANCELED");
        verify(groupBuyRepository, never()).save(any(GroupBuy.class));
    }

    @Test
    @DisplayName("참여자 수 존재 - 409 예외")
    void deleteGroupBuy_has_participants() {
        when(groupBuyRepository.findById(1L))
                .thenReturn(Optional.of(before));
        when(before.getPostStatus()).thenReturn("OPEN");
        when(before.getDueDate())
                .thenReturn(now.plusDays(1));
        when(orderRepository.countByGroupBuyIdAndStatusNot(1L, "CANCELED"))
                .thenReturn(1);

        assertThatThrownBy(() -> deleteGroupBuy.deleteGroupBuy(hostUser, 1L))
                .isInstanceOf(GroupBuyInvalidStateException.class)
                .hasMessageContaining(EXIST_PARTICIPANT);

        verify(groupBuyRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).countByGroupBuyIdAndStatusNot(1L, "CANCELED");
        verify(groupBuyRepository, never()).save(any(GroupBuy.class));
    }

    @Test
    @DisplayName("공구글 작성자가 아님 - 403 예외") // 이 로직을 두번째 상단으로 리팩토링할 필요 있음
    void deleteGroupBuy_notHost() {
        User otherUser = User.builder().id(2L).build();
        when(groupBuyRepository.findById(1L))
                .thenReturn(Optional.of(before));
        when(before.getPostStatus()).thenReturn("OPEN");
        when(before.getDueDate())
                .thenReturn(now.plusDays(1));
        when(orderRepository.countByGroupBuyIdAndStatusNot(1L, "CANCELED"))
                .thenReturn(0);
        when(before.getUser()).thenReturn(hostUser);

        assertThatThrownBy(() -> deleteGroupBuy.deleteGroupBuy(otherUser, 1L))
                .isInstanceOf(GroupBuyNotHostException.class)
                .hasMessageContaining(NOT_HOST);

        verify(groupBuyRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).countByGroupBuyIdAndStatusNot(1L, "CANCELED");
        verify(groupBuyRepository, never()).save(any(GroupBuy.class));
    }
}
