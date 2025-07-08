package com.moogsan.moongsan_backend.unit.groupbuy.service.query;

import com.moogsan.moongsan_backend.domain.chatting.participant.entity.ChatRoom;
import com.moogsan.moongsan_backend.domain.chatting.participant.repository.ChatRoomRepository;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.HostedList.HostedListResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.PagedResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.groupbuy.mapper.GroupBuyQueryMapper;
import com.moogsan.moongsan_backend.domain.groupbuy.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyQueryService.GetGroupBuyHostedList;
import com.moogsan.moongsan_backend.domain.groupbuy.util.FetchWishUtil;
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

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GetGroupBuyHostedListTest {

    @Mock
    private GroupBuyRepository groupBuyRepository;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private GroupBuyQueryMapper groupBuyQueryMapper;

    @Mock
    private FetchWishUtil fetchWishUtil;

    private GetGroupBuyHostedList getGroupBuyHostedList;
    private User loginedUser;
    private Pageable expectedPageable;
    private Long cursorId;
    private String postStatus;
    private int limit;
    private GroupBuy groupBuy1;
    private GroupBuy groupBuy2;
    private List<GroupBuy> groupBuys;
    private Map<Long, Boolean> wishMap;
    private List<Long> groupBuyIds;
    private List<ChatRoom> chatRooms;
    private List<HostedListResponse> posts;
    private HostedListResponse post1;
    private HostedListResponse post2;

    @BeforeEach
    void setUp() {
        loginedUser = User.builder().id(1L).build();
        postStatus = "OPEN";

        groupBuy1 = GroupBuy.builder().id(1L).user(loginedUser).build();
        groupBuy2 = GroupBuy.builder().id(2L).user(loginedUser).build();
        groupBuys = List.of(groupBuy1, groupBuy2);
        wishMap = mock(Map.class);
        groupBuyIds = List.of(1L, 2L);
        chatRooms = mock(List.class);
        post1 = HostedListResponse.builder().postId(1L).build();
        post2 = HostedListResponse.builder().postId(2L).build();
        posts = List.of(post2, post1);

        getGroupBuyHostedList = new GetGroupBuyHostedList(
                groupBuyRepository,
                chatRoomRepository,
                groupBuyQueryMapper,
                fetchWishUtil
        );
    }

    @Test
    @DisplayName("주최 공구 리스트 조회 성공 - 로그인한 유저, 커서 존재, 다음 요소 있음")
    void getGroupBuyHostedList_success_logined_user_cursor_has_next() {
        cursorId = 2L;
        limit = 1;
        expectedPageable = PageRequest.of(0, limit + 1, Sort.by("id").descending());

        when(groupBuyRepository.findByUser_IdAndPostStatusAndIdLessThan(loginedUser.getId(), postStatus, cursorId, expectedPageable))
                .thenReturn(groupBuys);
        when(fetchWishUtil.fetchWishMap(loginedUser.getId(), groupBuys)).thenReturn(wishMap);
        when(chatRoomRepository.findByGroupBuy_IdInAndType(groupBuyIds, "PARTICIPANT")).thenReturn(chatRooms);
        when(groupBuyQueryMapper.toHostedListWishResponses(groupBuys, wishMap, chatRooms)).thenReturn(posts);

        PagedResponse<HostedListResponse> result = getGroupBuyHostedList.getGroupBuyHostedList(loginedUser.getId(), postStatus, cursorId, limit);

        verify(groupBuyRepository, times(1)).findByUser_IdAndPostStatusAndIdLessThan(loginedUser.getId(), postStatus, cursorId, expectedPageable);
        verify(fetchWishUtil, times(1)).fetchWishMap(loginedUser.getId(), groupBuys);
        verify(chatRoomRepository, times(1)).findByGroupBuy_IdInAndType(groupBuyIds, "PARTICIPANT");
        verify(groupBuyQueryMapper, times(1)).toHostedListWishResponses(groupBuys, wishMap, chatRooms);
        assertThat(result.getPosts()).hasSize(1);
        assertThat(result.getNextCursor()).isEqualTo(2L);
        assertThat(result.isHasMore()).isTrue();
    }

    @Test
    @DisplayName("주최 공구 리스트 조회 성공 - 로그인한 유저, 커서 존재, 다음 요소 없음")
    void getGroupBuyHostedList_success_logined_user_cursor() {
        cursorId = 2L;
        limit = 2;
        expectedPageable = PageRequest.of(0, limit + 1, Sort.by("id").descending());

        when(groupBuyRepository.findByUser_IdAndPostStatusAndIdLessThan(loginedUser.getId(), postStatus, cursorId, expectedPageable))
                .thenReturn(groupBuys);
        when(fetchWishUtil.fetchWishMap(loginedUser.getId(), groupBuys)).thenReturn(wishMap);
        when(chatRoomRepository.findByGroupBuy_IdInAndType(groupBuyIds, "PARTICIPANT")).thenReturn(chatRooms);
        when(groupBuyQueryMapper.toHostedListWishResponses(groupBuys, wishMap, chatRooms)).thenReturn(posts);

        PagedResponse<HostedListResponse> result = getGroupBuyHostedList.getGroupBuyHostedList(loginedUser.getId(), postStatus, cursorId, limit);

        verify(groupBuyRepository, times(1)).findByUser_IdAndPostStatusAndIdLessThan(loginedUser.getId(), postStatus, cursorId, expectedPageable);
        verify(fetchWishUtil, times(1)).fetchWishMap(loginedUser.getId(), groupBuys);
        verify(chatRoomRepository, times(1)).findByGroupBuy_IdInAndType(groupBuyIds, "PARTICIPANT");
        verify(groupBuyQueryMapper, times(1)).toHostedListWishResponses(groupBuys, wishMap, chatRooms);
        assertThat(result.getPosts()).hasSize(2);
        assertThat(result.getNextCursor()).isEqualTo(1L);
        assertThat(result.isHasMore()).isFalse();
    }

    @Test
    @DisplayName("주최 공구 리스트 조회 성공 - 로그인한 유저, 커서 미존재, 다음 요소 있음")
    void getGroupBuyHostedList_success_logined_user_has_next() {
        limit = 1;
        cursorId = null;
        expectedPageable = PageRequest.of(0, limit + 1, Sort.by("id").descending());

        when(groupBuyRepository.findByUser_IdAndPostStatus(loginedUser.getId(), postStatus, expectedPageable))
                .thenReturn(groupBuys);
        when(fetchWishUtil.fetchWishMap(loginedUser.getId(), groupBuys)).thenReturn(wishMap);
        when(chatRoomRepository.findByGroupBuy_IdInAndType(groupBuyIds, "PARTICIPANT")).thenReturn(chatRooms);
        when(groupBuyQueryMapper.toHostedListWishResponses(groupBuys, wishMap, chatRooms)).thenReturn(posts);

        PagedResponse<HostedListResponse> result = getGroupBuyHostedList.getGroupBuyHostedList(loginedUser.getId(), postStatus, cursorId, limit);

        verify(groupBuyRepository, times(1)).findByUser_IdAndPostStatus(loginedUser.getId(), postStatus, expectedPageable);
        verify(fetchWishUtil, times(1)).fetchWishMap(loginedUser.getId(), groupBuys);
        verify(chatRoomRepository, times(1)).findByGroupBuy_IdInAndType(groupBuyIds, "PARTICIPANT");
        verify(groupBuyQueryMapper, times(1)).toHostedListWishResponses(groupBuys, wishMap, chatRooms);
        assertThat(result.getPosts()).hasSize(1);
        assertThat(result.getNextCursor()).isEqualTo(2L);
        assertThat(result.isHasMore()).isTrue();
    }

    @Test
    @DisplayName("주최 공구 리스트 조회 성공 - 로그인한 유저, 커서 미존재, 다음 요소 없음")
    void getGroupBuyHostedList_success_logined_user() {
        limit = 3;
        cursorId = null;
        expectedPageable = PageRequest.of(0, limit + 1, Sort.by("id").descending());

        when(groupBuyRepository.findByUser_IdAndPostStatus(loginedUser.getId(), postStatus, expectedPageable))
                .thenReturn(groupBuys);
        when(fetchWishUtil.fetchWishMap(loginedUser.getId(), groupBuys)).thenReturn(wishMap);
        when(chatRoomRepository.findByGroupBuy_IdInAndType(groupBuyIds, "PARTICIPANT")).thenReturn(chatRooms);
        when(groupBuyQueryMapper.toHostedListWishResponses(groupBuys, wishMap, chatRooms)).thenReturn(posts);

        PagedResponse<HostedListResponse> result = getGroupBuyHostedList.getGroupBuyHostedList(loginedUser.getId(), postStatus, cursorId, limit);

        verify(groupBuyRepository, times(1)).findByUser_IdAndPostStatus(loginedUser.getId(), postStatus, expectedPageable);
        verify(fetchWishUtil, times(1)).fetchWishMap(loginedUser.getId(), groupBuys);
        verify(chatRoomRepository, times(1)).findByGroupBuy_IdInAndType(groupBuyIds, "PARTICIPANT");
        verify(groupBuyQueryMapper, times(1)).toHostedListWishResponses(groupBuys, wishMap, chatRooms);
        assertThat(result.getPosts()).hasSize(2);
        assertThat(result.isHasMore()).isFalse();
    }
}
