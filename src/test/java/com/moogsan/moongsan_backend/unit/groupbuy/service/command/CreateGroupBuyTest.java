package com.moogsan.moongsan_backend.unit.groupbuy.service.command;

import com.moogsan.moongsan_backend.image.application.service.ImageService;
import com.moogsan.moongsan_backend.participantchat.application.facade.command.ChattingCommandFacade;
import com.moogsan.moongsan_backend.groupbuy.presentation.dto.command.request.CreateGroupBuyRequest;
import com.moogsan.moongsan_backend.groupbuy.domain.entity.GroupBuy;
import com.moogsan.moongsan_backend.groupbuy.domain.exception.specific.GroupBuyInvalidStateException;
import com.moogsan.moongsan_backend.groupbuy.application.mapper.GroupBuyCommandMapper;
import com.moogsan.moongsan_backend.groupbuy.domain.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.groupbuy.application.service.command.CreateGroupBuy;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import com.moogsan.moongsan_backend.global.lock.DuplicateRequestPreventer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static com.moogsan.moongsan_backend.groupbuy.domain.message.ResponseMessage.NOT_DIVISOR;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CreateGroupBuyTest {

    @Mock private ImageService imageService;
    @Mock private GroupBuyCommandMapper groupBuyCommandMapper;
    @Mock private GroupBuyRepository groupBuyRepository;
    @Mock private ChattingCommandFacade chattingCommandFacade;
    @Mock private DuplicateRequestPreventer duplicateRequestPreventer;
    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;

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
                imageService,
                groupBuyCommandMapper,
                groupBuyRepository,
                chattingCommandFacade,
                duplicateRequestPreventer,
                redisTemplate,
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
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(groupBuyCommandMapper.create(request, user)).thenReturn(mockGb);
        when(groupBuyRepository.save(mockGb)).thenReturn(mockGb);
        doReturn(42L).when(mockGb).getId();

        // when
        Long result = createGroupBuy.createGroupBuy(user, request);

        // then
        verify(groupBuyCommandMapper, times(1)).create(request, user);
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
        verify(mockGb, never()).increaseParticipantCount();
        verify(groupBuyRepository, never()).save(mockGb);
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
        verify(mockGb, never()).increaseParticipantCount();
        verify(groupBuyRepository, never()).save(mockGb);
    }
}
