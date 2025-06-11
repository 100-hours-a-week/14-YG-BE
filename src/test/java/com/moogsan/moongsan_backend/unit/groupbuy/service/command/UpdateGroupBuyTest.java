package com.moogsan.moongsan_backend.unit.groupbuy.service.command;

import com.moogsan.moongsan_backend.domain.groupbuy.dto.command.request.CreateGroupBuyRequest;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.command.request.UpdateGroupBuyRequest;
import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyInvalidStateException;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyNotFoundException;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyNotHostException;
import com.moogsan.moongsan_backend.domain.groupbuy.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyCommandService.UpdateGroupBuy;
import com.moogsan.moongsan_backend.domain.image.entity.Image;
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
    private Image image;
    private GroupBuy gb;

    private UpdateGroupBuyRequest updateRequest;

    private GroupBuy.GroupBuyBuilder defaultGroupBuy() {
        return GroupBuy.builder()
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
                .dueDate(LocalDateTime.now().plusDays(3))
                .location("카카오테크 교육장")
                .pickupDate(LocalDateTime.now().plusDays(4))
                .images(List.of(image))
                .user(hostUser);
    }

    @BeforeEach
    void setup() {
        hostUser = User.builder().id(1L).build();

        image = Image.builder()
                .id(2L)
                .imageKey("images/1")
                .imageSeqNo(0)
                .thumbnail(true)
                .build();
    }

    @Test
    @DisplayName("공구 전체 수정 성공")
    void updateGroupBuy_success() {
        gb = defaultGroupBuy().build();

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

        when(groupBuyRepository.findById(20L))
                .thenReturn(Optional.of(gb));
        when(groupBuyRepository.save(any(GroupBuy.class)))
                .thenReturn(gb);

        Long id = updateGroupBuy.updateGroupBuy(hostUser, updateRequest, 20L);

        assertThat(id).isEqualTo(20L);
        assertThat(gb.getTitle()).isEqualTo(updateRequest.getTitle());
        assertThat(gb.getName()).isEqualTo(updateRequest.getName());
        assertThat(gb.getHostQuantity()).isEqualTo(updateRequest.getHostQuantity());
        assertThat(gb.getDescription()).isEqualTo(updateRequest.getDescription());
        assertThat(gb.getDueDate()).isEqualTo(updateRequest.getDueDate());
        assertThat(gb.getPickupDate()).isEqualTo(updateRequest.getPickupDate());
        assertThat(gb.getDateModificationReason()).isEqualTo(updateRequest.getDateModificationReason());
        ///  이미지 변경 여부도 확인 필요
        verify(groupBuyRepository).save(any(GroupBuy.class));
        verify(imageMapper).mapImagesToGroupBuy(
                eq(updateRequest.getImageKeys()),
                any(GroupBuy.class)
        );
    }

    @Test
    @DisplayName("존재하지 않는 공구글 - 404 ")
    void updateGroupBuy_notFound() {
        when(groupBuyRepository.findById(20L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateGroupBuy.updateGroupBuy(hostUser, updateRequest, 20L))
                .isInstanceOf(GroupBuyNotFoundException.class)
                .hasMessageContaining(NOT_EXIST);
    }

    @Test
    @DisplayName("공구글 status가 OPEN이 아님 - 409")
    void updateGroupBuy_postStatus_not_open() {
        gb = defaultGroupBuy()
                .postStatus("CLOSED")
                .build();
        when(groupBuyRepository.findById(20L))
                .thenReturn(Optional.of(gb));
        assertThatThrownBy(() -> updateGroupBuy.updateGroupBuy(hostUser, updateRequest, 20L))
                .isInstanceOf(GroupBuyInvalidStateException.class)
                .hasMessageContaining(NOT_OPEN);
    }

    @Test
    @DisplayName("dueDate가 현재보다 과거 - 409")
    void updateGroupBuy_dueDate_past() {
        gb = defaultGroupBuy()
                .dueDate(LocalDateTime.now().minusDays(1))
                .build();
        when(groupBuyRepository.findById(20L))
                .thenReturn(Optional.of(gb));

        assertThatThrownBy(() -> updateGroupBuy.updateGroupBuy(hostUser, updateRequest, 20L))
                .isInstanceOf(GroupBuyInvalidStateException.class)
                .hasMessageContaining(NOT_OPEN);
    }

    @Test
    @DisplayName("공구글 주최자가 아님 - 403")
    void updateGroupBuy_notHost() {
        gb = defaultGroupBuy().build();
        User otherUser = User.builder().id(2L).build();
        when(groupBuyRepository.findById(20L)).thenReturn(Optional.of(gb));

        assertThatThrownBy(() -> updateGroupBuy.updateGroupBuy(otherUser, updateRequest, 20L))
                .isInstanceOf(GroupBuyNotHostException.class)
                .hasMessageContaining(NOT_HOST);
    }
}
