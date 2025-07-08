package com.moogsan.moongsan_backend.unit.groupbuy.service.query;

import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyDetail.UserAccountResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyUpdate.GroupBuyForUpdateResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyNotHostException;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyNotParticipantException;
import com.moogsan.moongsan_backend.domain.groupbuy.mapper.GroupBuyQueryMapper;
import com.moogsan.moongsan_backend.domain.groupbuy.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyQueryService.GetGroupBuyHostAccountInfo;
import com.moogsan.moongsan_backend.domain.order.entity.Order;
import com.moogsan.moongsan_backend.domain.order.repository.OrderRepository;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.moogsan.moongsan_backend.domain.groupbuy.message.ResponseMessage.NOT_HOST;
import static com.moogsan.moongsan_backend.domain.groupbuy.message.ResponseMessage.NOT_PARTICIPANT;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GetGroupBuyHostAccountInfoTest {

    @Mock
    private GroupBuyRepository groupBuyRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private GroupBuyQueryMapper groupBuyQueryMapper;

    private GetGroupBuyHostAccountInfo getGroupBuyHostAccountInfo;
    private UserAccountResponse userAccountResponse;
    private GroupBuy groupBuy;
    private Order order;
    private User hostUser;
    private User participantUser;
    private User normalUser;

    @BeforeEach
    void setUp() {

        groupBuy = mock(GroupBuy.class);
        order = mock(Order.class);
        userAccountResponse = mock(UserAccountResponse.class);

        hostUser = User.builder().id(1L).build();
        participantUser = User.builder().id(2L).build();
        normalUser = User.builder().id(3L).build();

        getGroupBuyHostAccountInfo = new GetGroupBuyHostAccountInfo(
                groupBuyRepository,
                orderRepository,
                groupBuyQueryMapper
        );

    }

    @Test
    @DisplayName("공구 게시글 주최자 계좌 정보 조회 성공 - 주최자")
    void getGroupBuyHostAccountInfo_success_host() {
        when(groupBuyRepository.findById(20L)).thenReturn(Optional.ofNullable(groupBuy));
        when(groupBuy.getUser()).thenReturn(hostUser);
        when(groupBuyQueryMapper.toHostAccount(groupBuy))
                .thenReturn(userAccountResponse);

        // when
        UserAccountResponse result = getGroupBuyHostAccountInfo.getGroupBuyHostAccountInfo(hostUser.getId(), 20L);

        // then
        verify(groupBuyRepository, times(1)).findById(20L);
        verify(groupBuyQueryMapper, times(1))
                .toHostAccount(groupBuy);
        assertThat(result).isInstanceOf(UserAccountResponse.class);
    }

    @Test
    @DisplayName("공구 게시글 주최자 계좌 정보 조회 성공 - 참여자")
    void getGroupBuyHostAccountInfo_success_participant() {
        when(groupBuyRepository.findById(20L)).thenReturn(Optional.ofNullable(groupBuy));
        when(groupBuy.getUser()).thenReturn(hostUser);
        when(orderRepository.findByUserIdAndGroupBuyIdAndStatusNot(participantUser.getId(), groupBuy.getId(), "CANCELED"))
                .thenReturn(Optional.ofNullable(order));
        when(groupBuyQueryMapper.toHostAccount(groupBuy))
                .thenReturn(userAccountResponse);

        UserAccountResponse result = getGroupBuyHostAccountInfo.getGroupBuyHostAccountInfo(participantUser.getId(), 20L);

        verify(groupBuyRepository, times(1)).findById(20L);
        verify(orderRepository, times(1))
                .findByUserIdAndGroupBuyIdAndStatusNot(participantUser.getId(), groupBuy.getId(), "CANCELED");
        assertThat(result).isInstanceOf(UserAccountResponse.class);
        verify(groupBuyQueryMapper, times(1))
                .toHostAccount(groupBuy);
    }

    @Test
    @DisplayName("공구 게시글 주최자 계좌 정보 조회 실패 - 일반 유저")
    void getGroupBuyHostAccountInfo_fail_normal_user() {
        when(groupBuyRepository.findById(20L)).thenReturn(Optional.ofNullable(groupBuy));
        when(groupBuy.getUser()).thenReturn(hostUser);
        when(orderRepository.findByUserIdAndGroupBuyIdAndStatusNot(normalUser.getId(), groupBuy.getId(), "CANCELED"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> getGroupBuyHostAccountInfo.getGroupBuyHostAccountInfo(normalUser.getId(), 20L))
                .isInstanceOf(GroupBuyNotParticipantException.class)
                .hasMessageContaining(NOT_PARTICIPANT);

        verify(groupBuyRepository, times(1)).findById(20L);
        verify(orderRepository, times(1))
                .findByUserIdAndGroupBuyIdAndStatusNot(normalUser.getId(), groupBuy.getId(), "CANCELED");
        verify(groupBuyQueryMapper, never())
                .toHostAccount(groupBuy);
    }
}
