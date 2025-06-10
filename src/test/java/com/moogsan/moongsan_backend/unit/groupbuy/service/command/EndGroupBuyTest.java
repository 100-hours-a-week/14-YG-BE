package com.moogsan.moongsan_backend.unit.groupbuy.service.command;

import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyInvalidStateException;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyNotFoundException;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyNotHostException;
import com.moogsan.moongsan_backend.domain.groupbuy.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyCommandService.EndGroupBuy;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.moogsan.moongsan_backend.domain.groupbuy.message.ResponseMessage.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EndGroupBuyTest {
    @Mock
    private GroupBuyRepository groupBuyRepository;
    @InjectMocks
    private EndGroupBuy endGroupBuy;

    private User hostUser;
    private User participant;
    private GroupBuy before;

    @BeforeEach
    void setup() {
        hostUser = User.builder().id(2L).build();
        participant = User.builder().id(1L).build();
        before = mock(GroupBuy.class);
    }

    @Test
    @DisplayName("공구 종료 성공")
    void endGroupBuy_success() {
        when(groupBuyRepository.findById(1L))
                .thenReturn(Optional.of(before));
        when(before.getPostStatus())
                .thenReturn("CLOSED");
        when(before.getDueDate())
                .thenReturn(LocalDateTime.now().minusDays(1));
        when(before.getPickupDate())
                .thenReturn(LocalDateTime.now().minusDays(1));
        when(before.isFixed())
                .thenReturn(true);
        when(before.getUser()).thenReturn(hostUser);

        endGroupBuy.endGroupBuy(hostUser, 1L);

        verify(groupBuyRepository).save(any(GroupBuy.class));
    }

    @Test
    @DisplayName("존재하지 않는 공구글 - 404 예외")
    void endGroupBuy_groupBuy_notFound() {
        when(groupBuyRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> endGroupBuy.endGroupBuy(participant, 1L))
                .isInstanceOf(GroupBuyNotFoundException.class)
                .hasMessageContaining(NOT_EXIST);
    }

    @Test
    @DisplayName("공구글 status가 CLOSED가 아님 - 409 예외")
    void endGroupBuy_invalid_postStatus_CLOSED() {
        when(groupBuyRepository.findById(1L))
                .thenReturn(Optional.of(before));
        when(before.getPostStatus())
                .thenReturn("OPEN");

        assertThatThrownBy(() -> endGroupBuy.endGroupBuy(participant, 1L))
                .isInstanceOf(GroupBuyInvalidStateException.class)
                .hasMessageContaining(BEFORE_CLOSED);
    }

    @Test
    @DisplayName("공구글 status가 ENDED - 409 예외")
    void endGroupBuy_invalid_postStatus_ENDED() {
        when(groupBuyRepository.findById(1L))
                .thenReturn(Optional.of(before));
        when(before.getPostStatus())
                .thenReturn("ENDED");

        assertThatThrownBy(() -> endGroupBuy.endGroupBuy(participant, 1L))
                .isInstanceOf(GroupBuyInvalidStateException.class)
                .hasMessageContaining(AFTER_ENDED);
    }

    @Test
    @DisplayName("공구글 dueDate 지남 - 409 예외")
    void endGroupBuy_dueDate_past() {
        when(groupBuyRepository.findById(1L))
                .thenReturn(Optional.of(before));
        when(before.getPostStatus())
                .thenReturn("CLOSED");
        when(before.getDueDate())
                .thenReturn(LocalDateTime.now().plusDays(1));

        assertThatThrownBy(() -> endGroupBuy.endGroupBuy(participant, 1L))
                .isInstanceOf(GroupBuyInvalidStateException.class)
                .hasMessageContaining(BEFORE_CLOSED);
    }

    @Test
    @DisplayName("공구글 pickupDate 지남 - 409 예외")
    void endGroupBuy_pickupDate_past() {
        when(groupBuyRepository.findById(1L))
                .thenReturn(Optional.of(before));
        when(before.getPostStatus())
                .thenReturn("CLOSED");
        when(before.getDueDate())
                .thenReturn(LocalDateTime.now().minusDays(1));
        when(before.getPickupDate())
                .thenReturn(LocalDateTime.now().plusDays(1));

        assertThatThrownBy(() -> endGroupBuy.endGroupBuy(participant, 1L))
                .isInstanceOf(GroupBuyInvalidStateException.class)
                .hasMessageContaining(BEFORE_PICKUP_DATE);
    }

    @Test
    @DisplayName("공구글 fixed 아님 - 409 예외")
    void endGroupBuy_not_fixed() {
        when(groupBuyRepository.findById(1L))
                .thenReturn(Optional.of(before));
        when(before.getPostStatus())
                .thenReturn("CLOSED");
        when(before.getDueDate())
                .thenReturn(LocalDateTime.now().minusDays(1));
        when(before.getPickupDate())
                .thenReturn(LocalDateTime.now().minusDays(1));
        when(before.isFixed())
                .thenReturn(false);

        assertThatThrownBy(() -> endGroupBuy.endGroupBuy(participant, 1L))
                .isInstanceOf(GroupBuyInvalidStateException.class)
                .hasMessageContaining(BEFORE_FIXED);
    }

    @Test
    @DisplayName("주최자 아님 - 404 예외")
    void endGroupBuy_not_host() {
        when(groupBuyRepository.findById(1L))
                .thenReturn(Optional.of(before));
        when(before.getPostStatus())
                .thenReturn("CLOSED");
        when(before.getDueDate())
                .thenReturn(LocalDateTime.now().minusDays(1));
        when(before.getPickupDate())
                .thenReturn(LocalDateTime.now().minusDays(1));
        when(before.isFixed())
                .thenReturn(true);
        when(before.getUser()).thenReturn(hostUser);

        assertThatThrownBy(() -> endGroupBuy.endGroupBuy(participant, 1L))
                .isInstanceOf(GroupBuyNotHostException.class)
                .hasMessageContaining(NOT_HOST);
    }
}
