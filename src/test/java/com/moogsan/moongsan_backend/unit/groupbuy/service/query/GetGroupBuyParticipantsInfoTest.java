package com.moogsan.moongsan_backend.unit.groupbuy.service.query;

import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyDetail.DetailResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.ParticipantList.ParticipantListResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.ParticipantList.ParticipantResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyNotFoundException;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyNotHostException;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyNotParticipantException;
import com.moogsan.moongsan_backend.domain.groupbuy.mapper.GroupBuyQueryMapper;
import com.moogsan.moongsan_backend.domain.groupbuy.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyQueryService.GetGroupBuyParticipantsInfo;
import com.moogsan.moongsan_backend.domain.order.entity.Order;
import com.moogsan.moongsan_backend.domain.order.repository.OrderRepository;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.moogsan.moongsan_backend.domain.groupbuy.message.ResponseMessage.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class GetGroupBuyParticipantsInfoTest {

    @Mock
    private GroupBuyRepository groupBuyRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private GroupBuyQueryMapper groupBuyQueryMapper;

    private GetGroupBuyParticipantsInfo getGroupBuyParticipantsInfo;
    private User hostUser;
    private User normalUser1;
    private User normalUser2;
    private GroupBuy groupBuy;
    private Order order1;
    private Order order2;
    private List<Order> orders;

    @BeforeEach
    void setUp() {

        hostUser = User.builder().id(1L).build();
        normalUser1 = User.builder().id(2L).build();
        normalUser2 = User.builder().id(3L).build();
        groupBuy = GroupBuy.builder().id(1L).user(hostUser).build();
        order1 = Order.builder().id(2L).user(normalUser1).build();
        order2 = Order.builder().id(3L).user(normalUser2).build();
        orders = List.of(order1, order2);

        getGroupBuyParticipantsInfo = new GetGroupBuyParticipantsInfo(
                groupBuyRepository,
                orderRepository,
                groupBuyQueryMapper
        );
    }

    @Test
    @DisplayName("공구 참여자 목록 조회 성공 - 주최자")
    void getGroupBuyDetail_success_host() {
        ParticipantResponse respA = mock(ParticipantResponse.class);
        ParticipantResponse respB = mock(ParticipantResponse.class);

        when(groupBuyRepository.findById(1L)).thenReturn(Optional.ofNullable(groupBuy));
        when(orderRepository.findByGroupBuyIdAndStatusNot(hostUser.getId(), "CANCELED")).thenReturn(orders);
        when(groupBuyQueryMapper.toParticipantResponse(order1)).thenReturn(respA);
        when(groupBuyQueryMapper.toParticipantResponse(order2)).thenReturn(respB);

        ParticipantListResponse result = getGroupBuyParticipantsInfo.getGroupBuyParticipantsInfo(hostUser.getId(), 1L);

        verify(groupBuyRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).findByGroupBuyIdAndStatusNot(hostUser.getId(), "CANCELED");
        verify(groupBuyQueryMapper, times(1)).toParticipantResponse(order1);
        verify(groupBuyQueryMapper, times(1)).toParticipantResponse(order2);
        assertThat(result).isInstanceOf(ParticipantListResponse.class);
    }

    @Test
    @DisplayName("공구 참여자 목록 조회 실패 - 공구글 없음")
    void getGroupBuyDetail_success_not_found() {
        when(groupBuyRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getGroupBuyParticipantsInfo.getGroupBuyParticipantsInfo(hostUser.getId(), 1L))
                .isInstanceOf(GroupBuyNotFoundException.class)
                .hasMessageContaining(NOT_EXIST);

        verify(groupBuyRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("공구 참여자 목록 조회 실패 - 주최자 아님")
    void getGroupBuyDetail_success_not_host() {
        when(groupBuyRepository.findById(1L)).thenReturn(Optional.ofNullable(groupBuy));

        assertThatThrownBy(() -> getGroupBuyParticipantsInfo.getGroupBuyParticipantsInfo(normalUser1.getId(), 1L))
                .isInstanceOf(GroupBuyNotHostException.class)
                .hasMessageContaining(NOT_HOST);

        verify(groupBuyRepository, times(1)).findById(1L);
    }
}
