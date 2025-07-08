package com.moogsan.moongsan_backend.unit.groupbuy.service.command;

import com.moogsan.moongsan_backend.domain.chatting.participant.Facade.command.ChattingCommandFacade;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.command.request.CreateGroupBuyRequest;
import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyInvalidStateException;
import com.moogsan.moongsan_backend.domain.groupbuy.mapper.GroupBuyCommandMapper;
import com.moogsan.moongsan_backend.domain.groupbuy.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyCommandService.CreateGroupBuy;
import com.moogsan.moongsan_backend.domain.image.mapper.ImageMapper;
import com.moogsan.moongsan_backend.domain.image.service.S3Service;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import com.moogsan.moongsan_backend.global.lock.DuplicateRequestPreventer;
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

import static com.moogsan.moongsan_backend.domain.groupbuy.message.ResponseMessage.NOT_DIVISOR;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CreateGroupBuyTest {

    @Mock
    private DuplicateRequestPreventer duplicateRequestPreventer;

    @Mock
    private GroupBuyRepository groupBuyRepository;

    @Mock
    private ImageMapper imageMapper;

    @Mock
    private GroupBuyCommandMapper groupBuyCommandMapper;

    @Mock
    private ChattingCommandFacade chattingCommandFacade;

    @Mock
    private S3Service s3Service;

    private CreateGroupBuy createGroupBuy;
    private CreateGroupBuyRequest request;
    private User user;
    private Clock fixedClock;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        when(duplicateRequestPreventer.tryAcquireLock(anyString(), anyLong()))
                .thenReturn(true);

        fixedClock = Clock.fixed(
                Instant.parse("2025-06-11T13:00:00Z"),
                ZoneId.of("Asia/Seoul")
        );

        now = LocalDateTime.now(fixedClock);

        createGroupBuy = new CreateGroupBuy(
                groupBuyRepository,
                imageMapper,
                groupBuyCommandMapper,
                chattingCommandFacade,
                duplicateRequestPreventer,
                s3Service,
                fixedClock
        );

        request = CreateGroupBuyRequest.builder()
                .title("라면 공구")
                .name("진라면")
                .url("https://example.com")
                .price(10000)
                .totalAmount(100)
                .unitAmount(10)
                .hostQuantity(1)
                .description("라면 맛있어요")
                .dueDate(now.plusDays(3))
                .location("카카오테크 교육장")
                .pickupDate(now.plusDays(4))
                .imageKeys(List.of("tmp/image1.jpg"))
                .build();

        user = User.builder().id(1L).build();
    }

    @Test
    @DisplayName("공구 게시글 생성 성공")
    void createGroupBuy_success() {
        // given
        GroupBuy mockGb = mock(GroupBuy.class);
        when(groupBuyCommandMapper.create(request, user)).thenReturn(mockGb);
        when(groupBuyRepository.save(mockGb)).thenReturn(mockGb);
        doReturn(42L).when(mockGb).getId();
        when(chattingCommandFacade.joinChatRoom(user, mockGb.getId())).thenReturn(2L);

        // when
        Long result = createGroupBuy.createGroupBuy(user, request);

        // then
        verify(groupBuyCommandMapper, times(1)).create(request, user);
        verify(s3Service).moveImage(eq("tmp/image1.jpg"), eq("group-buys/image1.jpg"));
        verify(imageMapper).mapImagesToGroupBuy(eq(List.of("group-buys/image1.jpg")), eq(mockGb));
        verify(imageMapper).mapImagesToGroupBuy(eq(List.of("group-buys/image1.jpg")), eq(mockGb));
        verify(mockGb, times(1)).increaseParticipantCount();
        verify(groupBuyRepository, times(1)).save(mockGb);
        verify(chattingCommandFacade, times(1)).joinChatRoom(user, mockGb.getId());
        assertThat(result).isEqualTo(42L);
    }

    @Test
    @DisplayName("공구 게시글 생성 실패 - 단위 수량은 0이 될 수 없음")
    void createGroupBuy_invalid_unitAmount_zero() {
        GroupBuy mockGb = mock(GroupBuy.class);
        request.setUnitAmount(0);

        assertThatThrownBy(() -> createGroupBuy.createGroupBuy(user, request))
                .isInstanceOf(GroupBuyInvalidStateException.class)
                .hasMessageContaining(NOT_DIVISOR);

        verify(groupBuyCommandMapper, never()).create(request, user);
        verify(imageMapper, never()).mapImagesToGroupBuy(request.getImageKeys(), mockGb);
        verify(mockGb, never()).increaseParticipantCount();
        verify(groupBuyRepository, never()).save(mockGb);
        verify(chattingCommandFacade, never()).joinChatRoom(user, mockGb.getId());

    }

    @Test
    @DisplayName("공구 게시글 생성 실패 - 단위 수량은 총 상품 수량의 약수만 가능")
    void createGroupBuy_invalid_unitAmount() {
        GroupBuy mockGb = mock(GroupBuy.class);
        request.setUnitAmount(7);

        assertThatThrownBy(() -> createGroupBuy.createGroupBuy(user, request))
                .isInstanceOf(GroupBuyInvalidStateException.class)
                .hasMessageContaining(NOT_DIVISOR);

        verify(groupBuyCommandMapper, never()).create(request, user);
        verify(imageMapper, never()).mapImagesToGroupBuy(request.getImageKeys(), mockGb);
        verify(mockGb, never()).increaseParticipantCount();
        verify(groupBuyRepository, never()).save(mockGb);
        verify(chattingCommandFacade, never()).joinChatRoom(user, mockGb.getId());
    }
}
