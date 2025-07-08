package com.moogsan.moongsan_backend.unit.groupbuy.service.query;

import com.moogsan.moongsan_backend.domain.chatting.anonymous.service.GenerateAliasIdService;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyDetail.DetailResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.groupbuy.mapper.GroupBuyQueryMapper;
import com.moogsan.moongsan_backend.domain.groupbuy.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyQueryService.GetGroupBuyDetailInfo;
import com.moogsan.moongsan_backend.domain.image.entity.Image;
import com.moogsan.moongsan_backend.domain.order.repository.OrderRepository;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import com.moogsan.moongsan_backend.domain.user.repository.WishRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GetGroupBuyDetailInfoTest {

    @Mock
    private GroupBuyRepository groupBuyRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private GroupBuyQueryMapper groupBuyQueryMapper;

    @Mock
    private WishRepository wishRepository;

    @Mock
    private GenerateAliasIdService generateAliasIdService;

    private GetGroupBuyDetailInfo getGroupBuyDetailInfo;
    private User hostUser;
    private User participantUser;
    private User normalUser;
    private GroupBuy groupBuy;
    private Image image;
    private Clock fixedClock;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        hostUser = User.builder().id(1L).build();
        participantUser = User.builder().id(2L).build();
        normalUser = User.builder().id(3L).build();

        fixedClock = Clock.fixed(
                Instant.parse("2025-06-11T13:00:00Z"),
                ZoneId.of("Asia/Seoul")
        );

        now = LocalDateTime.now(fixedClock);

        getGroupBuyDetailInfo = new GetGroupBuyDetailInfo(
                groupBuyRepository,
                orderRepository,
                groupBuyQueryMapper,
                wishRepository,
                fixedClock,
                generateAliasIdService
        );

        image = Image.builder()
                .id(2L)
                .imageKey("images/1")
                .imageSeqNo(0)
                .thumbnail(true)
                .build();

        groupBuy = GroupBuy.builder()
                .id(20L)
                .title("라면 공구")
                .name("진라면")
                .url("https://example.com")
                .price(10000)
                .unitPrice(100)
                .totalAmount(100)
                .unitAmount(10)
                .hostQuantity(1)
                .description("라면 맛있어요")
                .dueDate(now.plusDays(5))
                .location("카카오테크 교육장")
                .pickupDate(now.plusDays(7))
                .images(List.of(image))
                .user(hostUser)
                .build();
    }

    @Test
    @DisplayName("공구 게시글 상세 조회 성공 - 주최자")
    void getGroupBuyDetail_success_host() {
        DetailResponse detailResponse = mock(DetailResponse.class);

        when(groupBuyRepository.findWithImagesById(20L)).thenReturn(Optional.ofNullable(groupBuy));
        when(orderRepository.existsParticipant(hostUser.getId(), groupBuy.getId(), "CANCELED")).thenReturn(false);
        when(wishRepository.existsByUserIdAndGroupBuyId(hostUser.getId(), groupBuy.getId())).thenReturn(false);
        when(groupBuyQueryMapper.toDetailResponse(groupBuy, true, false, false, 0))
                .thenReturn(detailResponse);

        DetailResponse result = getGroupBuyDetailInfo.getGroupBuyDetailInfo(hostUser.getId(), 20L);

        verify(groupBuyRepository, times(1)).findWithImagesById(20L);
        verify(orderRepository, times(1)).existsParticipant(hostUser.getId(), groupBuy.getId(), "CANCELED");
        verify(wishRepository, times(1)).existsByUserIdAndGroupBuyId(hostUser.getId(), groupBuy.getId());
        verify(groupBuyQueryMapper, times(1))
                .toDetailResponse(groupBuy, true, false, false, 0);
        assertThat(result).isInstanceOf(DetailResponse.class);
    }

    @Test
    @DisplayName("공구 게시글 상세 조회 성공 - 참가자")
    void getGroupBuyDetail_success_participant() {
        DetailResponse detailResponse = mock(DetailResponse.class);

        when(groupBuyRepository.findWithImagesById(20L)).thenReturn(Optional.ofNullable(groupBuy));
        when(orderRepository.existsParticipant(participantUser.getId(), groupBuy.getId(), "CANCELED")).thenReturn(true);
        when(wishRepository.existsByUserIdAndGroupBuyId(participantUser.getId(), groupBuy.getId())).thenReturn(true);
        when(groupBuyQueryMapper.toDetailResponse(groupBuy, false, true, true, 0))
                .thenReturn(detailResponse);

        DetailResponse result = getGroupBuyDetailInfo.getGroupBuyDetailInfo(participantUser.getId(), 20L);

        verify(groupBuyRepository, times(1)).findWithImagesById(20L);
        verify(orderRepository, times(1)).existsParticipant(participantUser.getId(), groupBuy.getId(), "CANCELED");
        verify(wishRepository, times(1)).existsByUserIdAndGroupBuyId(participantUser.getId(), groupBuy.getId());
        verify(groupBuyQueryMapper, times(1))
                .toDetailResponse(groupBuy, false, true, true, 0);
        assertThat(result).isInstanceOf(DetailResponse.class);
    }

    @Test
    @DisplayName("공구 게시글 상세 조회 성공 - 비참가자")
    void getGroupBuyDetail_success_user() {
        DetailResponse detailResponse = mock(DetailResponse.class);

        when(groupBuyRepository.findWithImagesById(20L)).thenReturn(Optional.ofNullable(groupBuy));
        when(orderRepository.existsParticipant(normalUser.getId(), groupBuy.getId(), "CANCELED")).thenReturn(false);
        when(wishRepository.existsByUserIdAndGroupBuyId(normalUser.getId(), groupBuy.getId())).thenReturn(false);
        when(groupBuyQueryMapper.toDetailResponse(groupBuy, false, false, false, 0))
                .thenReturn(detailResponse);

        DetailResponse result = getGroupBuyDetailInfo.getGroupBuyDetailInfo(normalUser.getId(), 20L);

        verify(groupBuyRepository, times(1)).findWithImagesById(20L);
        verify(orderRepository, times(1)).existsParticipant(normalUser.getId(), groupBuy.getId(), "CANCELED");
        verify(wishRepository, times(1)).existsByUserIdAndGroupBuyId(normalUser.getId(), groupBuy.getId());
        verify(groupBuyQueryMapper, times(1))
                .toDetailResponse(groupBuy, false, false, false, 0);
        assertThat(result).isInstanceOf(DetailResponse.class);
    }
}
