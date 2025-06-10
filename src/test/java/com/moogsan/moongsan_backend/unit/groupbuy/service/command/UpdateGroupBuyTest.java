package com.moogsan.moongsan_backend.unit.groupbuy.service.command;

import com.moogsan.moongsan_backend.domain.groupbuy.dto.command.request.UpdateGroupBuyRequest;
import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyInvalidStateException;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyNotFoundException;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyNotHostException;
import com.moogsan.moongsan_backend.domain.groupbuy.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyCommandService.UpdateGroupBuy;
import com.moogsan.moongsan_backend.domain.image.mapper.ImageMapper;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.moogsan.moongsan_backend.domain.groupbuy.message.ResponseMessage.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateGroupBuyTest {

    @Mock private GroupBuyRepository groupBuyRepository;
    @Mock private ImageMapper imageMapper;
    @InjectMocks private UpdateGroupBuy updateGroupBuy;

    private User hostUser;
    private GroupBuy before, after;
    private UpdateGroupBuyRequest updateRequest;

    @BeforeEach
    void setup() {
        hostUser = User.builder().id(1L).build();
        before = mock(GroupBuy.class);
        after  = mock(GroupBuy.class);

        updateRequest = UpdateGroupBuyRequest.builder()
                .title("라면 공구")
                .name("진라면")
                .hostQuantity(2)
                .description("라면 맛있어요")
                .dueDate(LocalDateTime.now().plusDays(3))
                .pickupDate(LocalDateTime.now().plusDays(10))
                .dateModificationReason("배송이 늦네요...")
                .imageKeys(List.of("images/image1.jpg"))
                .build();
    }

    @Test
    @DisplayName("공구 수정 성공")
    void updateGroupBuy_success() {
        when(groupBuyRepository.findById(1L))
                .thenReturn(Optional.of(before));
        when(before.getPostStatus()).thenReturn("OPEN");
        when(before.getDueDate()).thenReturn(LocalDateTime.now().plusDays(1));
        when(before.updateForm(eq(updateRequest))).thenReturn(after);
        when(before.getUser()).thenReturn(hostUser);
        when(groupBuyRepository.save(any(GroupBuy.class)))
                .thenReturn(after);
        when(after.getId()).thenReturn(1L);

        Long id = updateGroupBuy.updateGroupBuy(hostUser, updateRequest, 1L);

        assertThat(id).isEqualTo(1L);
        verify(groupBuyRepository).save(any(GroupBuy.class));
        verify(imageMapper).mapImagesToGroupBuy(
                eq(updateRequest.getImageKeys()),
                any(GroupBuy.class)
        );
    }

    @Test
    @DisplayName("존재하지 않는 공구글 - 404 ")
    void updateGroupBuy_notFound() {
        when(groupBuyRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateGroupBuy.updateGroupBuy(hostUser, updateRequest, 1L))
                .isInstanceOf(GroupBuyNotFoundException.class)
                .hasMessageContaining(NOT_EXIST);
    }

    @Test
    @DisplayName("공구글 status가 OPEN이 아님 - 409")
    void updateGroupBuy_postStatus_not_open() {
        when(groupBuyRepository.findById(1L))
                .thenReturn(Optional.of(before));
        when(before.getPostStatus()).thenReturn("CLOSED");

        assertThatThrownBy(() -> updateGroupBuy.updateGroupBuy(hostUser, updateRequest, 1L))
                .isInstanceOf(GroupBuyInvalidStateException.class)
                .hasMessageContaining(NOT_OPEN);
    }

    @Test
    @DisplayName("dueDate가 현재보다 과거 - 409")
    void updateGroupBuy_dueDate_past() {
        when(groupBuyRepository.findById(1L))
                .thenReturn(Optional.of(before));
        when(before.getPostStatus()).thenReturn("OPEN");
        when(before.getDueDate())
                .thenReturn(LocalDateTime.now().minusDays(1));

        assertThatThrownBy(() -> updateGroupBuy.updateGroupBuy(hostUser, updateRequest, 1L))
                .isInstanceOf(GroupBuyInvalidStateException.class)
                .hasMessageContaining(NOT_OPEN);
    }

    @Test
    @DisplayName("공구글 주최자가 아님 - 403")
    void updateGroupBuy_notHost() {
        User otherUser = User.builder().id(2L).build();
        when(groupBuyRepository.findById(1L)).thenReturn(Optional.of(before));
        when(before.getPostStatus()).thenReturn("OPEN");
        when(before.getDueDate()).thenReturn(LocalDateTime.now().plusDays(1));
        when(before.getUser()).thenReturn(hostUser);

        assertThatThrownBy(() -> updateGroupBuy.updateGroupBuy(otherUser, updateRequest, 1L))
                .isInstanceOf(GroupBuyNotHostException.class)
                .hasMessageContaining(NOT_HOST);
    }
}
