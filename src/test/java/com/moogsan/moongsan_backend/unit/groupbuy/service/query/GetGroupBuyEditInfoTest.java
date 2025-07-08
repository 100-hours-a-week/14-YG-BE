package com.moogsan.moongsan_backend.unit.groupbuy.service.query;

import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyDetail.DetailResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyUpdate.GroupBuyForUpdateResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyInvalidStateException;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyNotFoundException;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyNotHostException;
import com.moogsan.moongsan_backend.domain.groupbuy.mapper.GroupBuyQueryMapper;
import com.moogsan.moongsan_backend.domain.groupbuy.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyQueryService.GetGroupBuyEditInfo;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import org.assertj.core.api.Assertions;
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

import static com.moogsan.moongsan_backend.domain.groupbuy.message.ResponseMessage.*;
import static com.moogsan.moongsan_backend.domain.groupbuy.message.ResponseMessage.NOT_OPEN;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GetGroupBuyEditInfoTest {

    @Mock
    private GroupBuyRepository groupBuyRepository;

    @Mock
    private GroupBuyQueryMapper groupBuyQueryMapper;

    @Mock
    private Clock fixedClock;

    private GetGroupBuyEditInfo getGroupBuyEditInfo;
    private User hostUser;
    private User normalUser;
    private LocalDateTime now;
    private GroupBuy groupBuy;
    private GroupBuyForUpdateResponse groupBuyForUpdateResponse;

    @BeforeEach
    void setUp() {
        hostUser = User.builder().id(1L).build();
        normalUser = User.builder().id(2L).build();

        fixedClock = Clock.fixed(
                Instant.parse("2025-06-11T13:00:00Z"),
                ZoneId.of("Asia/Seoul")
        );

        now = LocalDateTime.now(fixedClock);

        getGroupBuyEditInfo = new GetGroupBuyEditInfo(
                groupBuyRepository,
                groupBuyQueryMapper,
                fixedClock
        );

        groupBuy = mock(GroupBuy.class);
        groupBuyForUpdateResponse = mock(GroupBuyForUpdateResponse.class);
    }

    @Test
    @DisplayName("공구 게시글 수정용 조회 성공 - 주최자")
    void getGetGroupBuyEditInfo_success_host() {
        when(groupBuyRepository.findWithImagesById(20L)).thenReturn(Optional.ofNullable(groupBuy));
        when(groupBuy.getPostStatus()).thenReturn("OPEN");
        when(groupBuy.getDueDate()).thenReturn(now.plusDays(3));
        when(groupBuy.getUser()).thenReturn(hostUser);
        when(groupBuyQueryMapper.toUpdateResponse(groupBuy))
                .thenReturn(groupBuyForUpdateResponse);

        // when
        GroupBuyForUpdateResponse result = getGroupBuyEditInfo.getGroupBuyEditInfo(hostUser.getId(), 20L);

        // then
        verify(groupBuyRepository, times(1)).findWithImagesById(20L);
        verify(groupBuyQueryMapper, times(1))
                .toUpdateResponse(groupBuy);
        assertThat(result).isInstanceOf(GroupBuyForUpdateResponse.class);
    }

    @Test
    @DisplayName("존재하지 않는 공구글 - 404 예외")
    void deleteGroupBuy_notFound() {
        when(groupBuyRepository.findWithImagesById(20L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getGroupBuyEditInfo.getGroupBuyEditInfo(hostUser.getId(), 20L))
                .isInstanceOf(GroupBuyNotFoundException.class)
                .hasMessageContaining(NOT_EXIST);

        verify(groupBuyRepository, times(1)).findWithImagesById(20L);
        verify(groupBuyQueryMapper, never()).toUpdateResponse(groupBuy);
    }

    @Test
    @DisplayName("공구 게시글 수정용 조회 실패 - 주최자가 아님")
    void getGetGroupBuyEditInfo_fail_not_host() {
        when(groupBuyRepository.findWithImagesById(20L)).thenReturn(Optional.ofNullable(groupBuy));
        when(groupBuy.getUser()).thenReturn(hostUser);

        assertThatThrownBy(() -> getGroupBuyEditInfo.getGroupBuyEditInfo(normalUser.getId(), 20L))
                .isInstanceOf(GroupBuyNotHostException.class)
                .hasMessageContaining(NOT_HOST);

        verify(groupBuyRepository, times(1)).findWithImagesById(20L);
        verify(groupBuyQueryMapper, never()).toUpdateResponse(groupBuy);
    }

    @Test
    @DisplayName("공구글 status가 OPEN이 아님 - 409 예외")
    void deleteGroupBuy_postStatus_not_open() {
        when(groupBuyRepository.findWithImagesById(20L)).thenReturn(Optional.ofNullable(groupBuy));
        when(groupBuy.getUser()).thenReturn(hostUser);
        when(groupBuy.getPostStatus()).thenReturn("CLOSED");

        assertThatThrownBy(() -> getGroupBuyEditInfo.getGroupBuyEditInfo(hostUser.getId(), 20L))
                .isInstanceOf(GroupBuyInvalidStateException.class)
                .hasMessageContaining(NOT_OPEN);

        verify(groupBuyRepository, times(1)).findWithImagesById(20L);
        verify(groupBuyQueryMapper, never()).toUpdateResponse(groupBuy);
    }

    @Test
    @DisplayName("dueDate가 현재보다 과거 - 409 예외")
    void deleteGroupBuy_dueDate_past() {
        when(groupBuyRepository.findWithImagesById(20L)).thenReturn(Optional.ofNullable(groupBuy));
        when(groupBuy.getUser()).thenReturn(hostUser);
        when(groupBuy.getPostStatus()).thenReturn("OPEN");
        when(groupBuy.getDueDate()).thenReturn(now.minusDays(3));

        assertThatThrownBy(() -> getGroupBuyEditInfo.getGroupBuyEditInfo(hostUser.getId(), 20L))
                .isInstanceOf(GroupBuyInvalidStateException.class)
                .hasMessageContaining(NOT_OPEN);

        verify(groupBuyRepository, times(1)).findWithImagesById(20L);
        verify(groupBuyQueryMapper, never()).toUpdateResponse(groupBuy);
    }
}
