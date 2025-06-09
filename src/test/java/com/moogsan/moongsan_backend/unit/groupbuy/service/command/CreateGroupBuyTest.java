package com.moogsan.moongsan_backend.unit.groupbuy.service.command;

import com.moogsan.moongsan_backend.domain.chatting.Facade.command.ChattingCommandFacade;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.command.request.CreateGroupBuyRequest;
import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.base.GroupBuyException;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyInvalidStateException;
import com.moogsan.moongsan_backend.domain.groupbuy.mapper.GroupBuyCommandMapper;
import com.moogsan.moongsan_backend.domain.groupbuy.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyCommandService.CreateGroupBuy;
import com.moogsan.moongsan_backend.domain.image.mapper.ImageMapper;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import com.moogsan.moongsan_backend.global.lock.DuplicateRequestPreventer;
import com.moogsan.moongsan_backend.support.fake.InMemoryDuplicateRequestPreventer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;
import java.util.List;

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

    @InjectMocks
    private CreateGroupBuy createGroupBuy;

    private CreateGroupBuyRequest request;
    private User user;

    @BeforeEach
    void setUp() {
        when(duplicateRequestPreventer.tryAcquireLock(anyString(), anyLong()))
                .thenReturn(true);

        request = CreateGroupBuyRequest.builder()
                .title("라면 공구")
                .name("진라면")
                .url("https://example.com")
                .price(10000)
                .totalAmount(100)
                .unitAmount(10)
                .hostQuantity(1)
                .description("라면 맛있어요")
                .dueDate(LocalDateTime.now().plusDays(3))
                .location("카카오테크 교육장")
                .pickupDate(LocalDateTime.now().plusDays(4))
                .imageKeys(List.of("images/image1.jpg"))
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
        verify(groupBuyCommandMapper).create(request, user);
        verify(imageMapper).mapImagesToGroupBuy(request.getImageKeys(), mockGb);
        verify(mockGb).increaseParticipantCount();
        verify(groupBuyRepository).save(mockGb);
        assertThat(result).isEqualTo(42L);
    }

    @Test
    @DisplayName("공구 게시글 생성 실패 - 단위 수량은 0이 될 수 없음")
    void createGroupBuy_invalid_unitAmount_zero() {
        request.setUnitAmount(0);

        assertThatThrownBy(() -> createGroupBuy.createGroupBuy(user, request))
                .isInstanceOf(GroupBuyInvalidStateException.class)
                .hasMessageContaining("상품 주문 단위는 상품 전체 수량의 약수여야 합니다.");
    }

    @Test
    @DisplayName("공구 게시글 생성 실패 - 단위 수량은 총 상품 수량의 약수만 가능")
    void createGroupBuy_invalid_unitAmount() {
        request.setUnitAmount(7);

        assertThatThrownBy(() -> createGroupBuy.createGroupBuy(user, request))
                .isInstanceOf(GroupBuyInvalidStateException.class)
                .hasMessageContaining("상품 주문 단위는 상품 전체 수량의 약수여야 합니다.");
    }
}