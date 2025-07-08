package com.moogsan.moongsan_backend.unit.groupbuy.service.query;

import com.moogsan.moongsan_backend.domain.chatting.participant.entity.ChatRoom;
import com.moogsan.moongsan_backend.domain.chatting.participant.repository.ChatRoomRepository;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.PagedResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.ParticipatedList.ParticipatedListResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.groupbuy.mapper.GroupBuyQueryMapper;
import com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyQueryService.GetGroupBuyParticipatedList;
import com.moogsan.moongsan_backend.domain.groupbuy.util.FetchWishUtil;
import com.moogsan.moongsan_backend.domain.order.entity.Order;
import com.moogsan.moongsan_backend.domain.order.repository.OrderRepository;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class GetGroupBuyParticipatedListTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private GroupBuyQueryMapper groupBuyQueryMapper;

    @Mock
    private FetchWishUtil fetchWishUtil;

    private GetGroupBuyParticipatedList getGroupBuyParticipatedList;
    private Pageable expectedPageable;
    private User loginedUser;
    private Long cursorId;
    private LocalDateTime cursorCreatedAt;
    private String postStatus;
    private int limit;
    private Order order1;
    private Order order2;
    private List<Order> orders;
    private GroupBuy groupBuy1;
    private GroupBuy groupBuy2;
    private List<GroupBuy> groupBuys;
    private Map<Long, Boolean> wishMap;
    private List<Long> groupBuyIds;
    private List<ChatRoom> chatRooms;
    private List<ParticipatedListResponse> posts;
    private ParticipatedListResponse post1;
    private ParticipatedListResponse post2;

    @BeforeEach
    void setUp(){
        loginedUser = User.builder().id(1L).build();
        postStatus = "OPEN";
        groupBuy1 = GroupBuy.builder().id(1L).user(loginedUser).build();
        groupBuy2 = GroupBuy.builder().id(2L).user(loginedUser).build();
        groupBuys = List.of(groupBuy2, groupBuy1);
        order1 = Order.builder().id(1L).groupBuy(groupBuy1).user(loginedUser).build();
        order2 = Order.builder().id(2L).groupBuy(groupBuy2).user(loginedUser).build();
        orders = List.of(order2, order1);
        wishMap = mock(Map.class);
        groupBuyIds = List.of(2L, 1L);
        chatRooms = mock(List.class);
        post1 = ParticipatedListResponse.builder().postId(1L).build();
        post2 = ParticipatedListResponse.builder().postId(2L).build();
        posts = List.of(post2, post1);

        getGroupBuyParticipatedList = new GetGroupBuyParticipatedList(
                orderRepository,
                chatRoomRepository,
                groupBuyQueryMapper,
                fetchWishUtil
        );
    }

    @Test
    @DisplayName("참여 공구 리스트 조회 성공 - 로그인한 유저, 커서 존재, 다음 요소 있음")
    void getGroupBuyParticipatedList_success_logined_user_cursor_has_next() {
        cursorId = 2L;
        cursorCreatedAt = LocalDateTime.of(2025, 6, 12, 3, 14);
        limit = 1;
        expectedPageable = PageRequest.of(
                0,
                limit + 1,
                Sort.by("createdAt").descending()
                        .and(Sort.by("id").descending())
        );

        when(orderRepository.findByUserAndPostStatusAndNotCanceledBeforeCursor(loginedUser.getId(), postStatus, cursorCreatedAt, cursorId, expectedPageable))
                .thenReturn(orders);
        when(fetchWishUtil.fetchWishMap(loginedUser.getId(), groupBuys)).thenReturn(wishMap);
        when(chatRoomRepository.findByGroupBuy_IdInAndType(groupBuyIds, "PARTICIPANT")).thenReturn(chatRooms);
        when(groupBuyQueryMapper.toParticipatedListWishResponse(orders, wishMap, chatRooms)).thenReturn(posts);

        PagedResponse<ParticipatedListResponse> result = getGroupBuyParticipatedList.getGroupBuyParticipatedList(loginedUser.getId(), postStatus, cursorCreatedAt, cursorId, limit);

        verify(orderRepository, times(1)).findByUserAndPostStatusAndNotCanceledBeforeCursor(loginedUser.getId(), postStatus, cursorCreatedAt, cursorId, expectedPageable);
        verify(fetchWishUtil, times(1)).fetchWishMap(loginedUser.getId(), groupBuys);
        verify(chatRoomRepository, times(1)).findByGroupBuy_IdInAndType(groupBuyIds, "PARTICIPANT");
        verify(groupBuyQueryMapper, times(1)).toParticipatedListWishResponse(orders, wishMap, chatRooms);
        assertThat(result.getPosts()).hasSize(1);
        assertThat(result.getNextCursor()).isEqualTo(2L);
        assertThat(result.isHasMore()).isTrue();
    }

    @Test
    @DisplayName("참여 공구 리스트 조회 성공 - 로그인한 유저, 커서 존재, 다음 요소 없음")
    void getGroupBuyParticipatedList_success_logined_user_cursor() {
        cursorId = 2L;
        cursorCreatedAt = LocalDateTime.of(2025, 6, 12, 3, 14);
        limit = 2;
        expectedPageable = PageRequest.of(
                0,
                limit + 1,
                Sort.by("createdAt").descending()
                        .and(Sort.by("id").descending())
        );

        when(orderRepository.findByUserAndPostStatusAndNotCanceledBeforeCursor(loginedUser.getId(), postStatus, cursorCreatedAt, cursorId, expectedPageable))
                .thenReturn(orders);
        when(fetchWishUtil.fetchWishMap(loginedUser.getId(), groupBuys)).thenReturn(wishMap);
        when(chatRoomRepository.findByGroupBuy_IdInAndType(groupBuyIds, "PARTICIPANT")).thenReturn(chatRooms);
        when(groupBuyQueryMapper.toParticipatedListWishResponse(orders, wishMap, chatRooms)).thenReturn(posts);

        PagedResponse<ParticipatedListResponse> result = getGroupBuyParticipatedList.getGroupBuyParticipatedList(loginedUser.getId(), postStatus, cursorCreatedAt, cursorId, limit);

        verify(orderRepository, times(1)).findByUserAndPostStatusAndNotCanceledBeforeCursor(loginedUser.getId(), postStatus, cursorCreatedAt, cursorId, expectedPageable);
        verify(fetchWishUtil, times(1)).fetchWishMap(loginedUser.getId(), groupBuys);
        verify(chatRoomRepository, times(1)).findByGroupBuy_IdInAndType(groupBuyIds, "PARTICIPANT");
        verify(groupBuyQueryMapper, times(1)).toParticipatedListWishResponse(orders, wishMap, chatRooms);
        assertThat(result.getPosts()).hasSize(2);
        assertThat(result.getNextCursor()).isEqualTo(1L);
        assertThat(result.isHasMore()).isFalse();
    }

    @Test
    @DisplayName("참여 공구 리스트 조회 성공 - 로그인한 유저, 커서 미존재, 다음 요소 있음")
    void getGroupBuyParticipatedList_success_logined_user_has_next() {
        cursorId = null;
        cursorCreatedAt = null;
        limit = 1;
        expectedPageable = PageRequest.of(
                0,
                limit + 1,
                Sort.by("createdAt").descending()
                        .and(Sort.by("id").descending())
        );

        when(orderRepository.findByUserAndPostStatusAndNotCanceled(loginedUser.getId(), postStatus, expectedPageable))
                .thenReturn(orders);
        when(fetchWishUtil.fetchWishMap(loginedUser.getId(), groupBuys)).thenReturn(wishMap);
        when(chatRoomRepository.findByGroupBuy_IdInAndType(groupBuyIds, "PARTICIPANT")).thenReturn(chatRooms);
        when(groupBuyQueryMapper.toParticipatedListWishResponse(orders, wishMap, chatRooms)).thenReturn(posts);

        PagedResponse<ParticipatedListResponse> result = getGroupBuyParticipatedList.getGroupBuyParticipatedList(loginedUser.getId(), postStatus, cursorCreatedAt, cursorId, limit);

        verify(orderRepository, times(1)).findByUserAndPostStatusAndNotCanceled(loginedUser.getId(), postStatus, expectedPageable);
        verify(fetchWishUtil, times(1)).fetchWishMap(loginedUser.getId(), groupBuys);
        verify(chatRoomRepository, times(1)).findByGroupBuy_IdInAndType(groupBuyIds, "PARTICIPANT");
        verify(groupBuyQueryMapper, times(1)).toParticipatedListWishResponse(orders, wishMap, chatRooms);
        assertThat(result.getPosts()).hasSize(1);
        assertThat(result.getNextCursor()).isEqualTo(2L);
        assertThat(result.isHasMore()).isTrue();
    }

    @Test
    @DisplayName("참여 공구 리스트 조회 성공 - 로그인한 유저, 커서 미존재, 다음 요소 없음")
    void getGroupBuyParticipatedList_success_logined_user() {
        cursorId = null;
        cursorCreatedAt = null;
        limit = 3;
        expectedPageable = PageRequest.of(
                0,
                limit + 1,
                Sort.by("createdAt").descending()
                        .and(Sort.by("id").descending())
        );

        when(orderRepository.findByUserAndPostStatusAndNotCanceled(loginedUser.getId(), postStatus, expectedPageable))
                .thenReturn(orders);
        when(fetchWishUtil.fetchWishMap(loginedUser.getId(), groupBuys)).thenReturn(wishMap);
        when(chatRoomRepository.findByGroupBuy_IdInAndType(groupBuyIds, "PARTICIPANT")).thenReturn(chatRooms);
        when(groupBuyQueryMapper.toParticipatedListWishResponse(orders, wishMap, chatRooms)).thenReturn(posts);

        PagedResponse<ParticipatedListResponse> result = getGroupBuyParticipatedList.getGroupBuyParticipatedList(loginedUser.getId(), postStatus, cursorCreatedAt, cursorId, limit);

        verify(orderRepository, times(1)).findByUserAndPostStatusAndNotCanceled(loginedUser.getId(), postStatus, expectedPageable);
        verify(fetchWishUtil, times(1)).fetchWishMap(loginedUser.getId(), groupBuys);
        verify(chatRoomRepository, times(1)).findByGroupBuy_IdInAndType(groupBuyIds, "PARTICIPANT");
        verify(groupBuyQueryMapper, times(1)).toParticipatedListWishResponse(orders, wishMap, chatRooms);
        assertThat(result.getPosts()).hasSize(2);
        assertThat(result.getNextCursor()).isEqualTo(1L);
        assertThat(result.isHasMore()).isFalse();
    }
}
