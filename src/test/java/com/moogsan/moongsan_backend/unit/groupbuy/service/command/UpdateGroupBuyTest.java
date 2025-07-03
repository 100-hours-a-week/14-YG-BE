package com.moogsan.moongsan_backend.unit.groupbuy.service.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moogsan.moongsan_backend.adapters.kafka.producer.mapper.GroupBuyEventMapper;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.command.request.CreateGroupBuyRequest;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.command.request.UpdateGroupBuyRequest;
import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyInvalidStateException;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyNotFoundException;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyNotHostException;
import com.moogsan.moongsan_backend.domain.groupbuy.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyCommandService.CreateGroupBuy;
import com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyCommandService.UpdateGroupBuy;
import com.moogsan.moongsan_backend.domain.image.entity.Image;
import com.moogsan.moongsan_backend.domain.image.mapper.ImageMapper;
import com.moogsan.moongsan_backend.domain.image.service.S3Service;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static com.moogsan.moongsan_backend.domain.groupbuy.message.ResponseMessage.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateGroupBuyTest {

    @Mock
    private GroupBuyRepository groupBuyRepository;

    @Mock
    private ImageMapper imageMapper;

    @Mock
    private S3Service s3Service;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private GroupBuyEventMapper eventMapper;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Clock clock;

    @InjectMocks
    private UpdateGroupBuy updateGroupBuy;

    private User hostUser;
    private Image image;
    private GroupBuy gb;
    private Clock fixedClock;
    private LocalDateTime now;

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
                .dueDate(now.plusDays(5))
                .location("카카오테크 교육장")
                .pickupDate(now.plusDays(7))
                .images(List.of(image))
                .user(hostUser);
    }

    @BeforeEach
    void setup() {

        hostUser = User.builder().id(1L).build();

        image = Image.builder()
                .id(2L)
                .imageKey("group-buys/image2.jpg")
                .imageSeqNo(0)
                .thumbnail(true)
                .build();

        fixedClock = Clock.fixed(
                Instant.parse("2025-06-11T13:00:00Z"),
                ZoneId.of("Asia/Seoul")
        );

        now = LocalDateTime.now(fixedClock);

        updateGroupBuy = new UpdateGroupBuy(
                groupBuyRepository,
                imageMapper,
                s3Service,
                kafkaTemplate,
                eventMapper,
                objectMapper,
                fixedClock
        );

        updateRequest = UpdateGroupBuyRequest.builder()
                .title("라면 공구합니다!!")
                .name("진라면 30봉")
                .hostQuantity(2)
                .description("라면 맛있어요. 같이 사실래요?")
                .dueDate(now.plusDays(10))
                .pickupDate(now.plusDays(12))
                .dateModificationReason("배송이 늦네요...")
                .imageKeys(List.of("tmp/image1.jpg"))
                .build();
    }

    @Test
    @DisplayName("공구 전체 수정 성공")
    void updateGroupBuy_success() {
        gb = defaultGroupBuy().build();

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
        verify(s3Service).deleteImage("group-buys/image2.jpg");
        verify(s3Service).moveImage("tmp/image1.jpg", "group-buys/image1.jpg");
        verify(imageMapper).mapImagesToGroupBuy(List.of("group-buys/image1.jpg"), gb);
    }

    @Test
    @DisplayName("존재하지 않는 공구글 - 404 ")
    void updateGroupBuy_notFound() {
        gb = defaultGroupBuy().build();

        when(groupBuyRepository.findById(20L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateGroupBuy.updateGroupBuy(hostUser, updateRequest, 20L))
                .isInstanceOf(GroupBuyNotFoundException.class)
                .hasMessageContaining(NOT_EXIST);

        verify(groupBuyRepository, never()).save(any(GroupBuy.class));
        verify(imageMapper, never()).mapImagesToGroupBuy(
                eq(updateRequest.getImageKeys()),
                any(GroupBuy.class)
        );
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

        verify(groupBuyRepository, never()).save(any(GroupBuy.class));
        verify(imageMapper, never()).mapImagesToGroupBuy(
                eq(updateRequest.getImageKeys()),
                any(GroupBuy.class)
        );
    }

    @Test
    @DisplayName("dueDate가 현재보다 과거 - 409")
    void updateGroupBuy_dueDate_past() {
        gb = defaultGroupBuy()
                .dueDate(now.minusDays(10))
                .build();
        when(groupBuyRepository.findById(20L))
                .thenReturn(Optional.of(gb));

        assertThatThrownBy(() -> updateGroupBuy.updateGroupBuy(hostUser, updateRequest, 20L))
                .isInstanceOf(GroupBuyInvalidStateException.class)
                .hasMessageContaining(NOT_OPEN);

        verify(groupBuyRepository, never()).save(any(GroupBuy.class));
        verify(imageMapper, never()).mapImagesToGroupBuy(
                eq(updateRequest.getImageKeys()),
                any(GroupBuy.class)
        );
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

        verify(groupBuyRepository, never()).save(any(GroupBuy.class));
        verify(imageMapper, never()).mapImagesToGroupBuy(
                eq(updateRequest.getImageKeys()),
                any(GroupBuy.class)
        );
    }
}
