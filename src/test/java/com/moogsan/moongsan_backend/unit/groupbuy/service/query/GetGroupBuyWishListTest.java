package com.moogsan.moongsan_backend.unit.groupbuy.service.query;

import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.PagedResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.WishList.WishListResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.groupbuy.mapper.GroupBuyQueryMapper;
import com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyQueryService.GetGroupBuyWishList;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import com.moogsan.moongsan_backend.domain.user.repository.WishRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GetGroupBuyWishListTest {

    @Mock
    private WishRepository wishRepository;

    @Mock
    private GroupBuyQueryMapper groupBuyQueryMapper;

    private GetGroupBuyWishList getGroupBuyWishList;

    // 공통 테스트 데이터
    private final User user = User.builder().id(7L).build();
    private final String postStatus = "OPEN";

    private GroupBuy groupBuy1;
    private GroupBuy groupBuy2;
    private WishListResponse res1;
    private WishListResponse res2;
    private List<GroupBuy> groupBuys;
    private List<WishListResponse> mapped;

    @BeforeEach
    void setUp() {
        groupBuy1 = GroupBuy.builder().id(1L).user(user).build();
        groupBuy2 = GroupBuy.builder().id(2L).user(user).build();
        groupBuys = List.of(groupBuy2, groupBuy1);

        res1 = WishListResponse.builder().postId(1L).build();
        res2 = WishListResponse.builder().postId(2L).build();
        mapped = List.of(res2, res1);

        getGroupBuyWishList = new GetGroupBuyWishList(groupBuyQueryMapper, wishRepository);
    }

    @Test
    @DisplayName("관심 공구 리스트 조회 - 커서 존재 & 다음 페이지 존재")
    void getWishList_cursor_hasNext() {
        int limit = 1;
        long cursorId = 2L;
        LocalDateTime cursorAt = LocalDateTime.of(2025, 6, 12, 13, 40);

        Pageable pageable = PageRequest.of(
                0, limit + 1,
                Sort.by("createdAt").descending().and(Sort.by("id").descending())
        );

        when(wishRepository.findGroupBuysByUserAndPostStatusBeforeCursor(
                user.getId(), postStatus, cursorAt, cursorId, pageable)
        ).thenReturn(groupBuys);

        when(groupBuyQueryMapper.toWishListResponse(groupBuy2)).thenReturn(res2);
        when(groupBuyQueryMapper.toWishListResponse(groupBuy1)).thenReturn(res1);

        PagedResponse<WishListResponse> result = getGroupBuyWishList.getGroupBuyWishList(
                user.getId(), postStatus, cursorAt, cursorId, limit);

        verify(wishRepository).findGroupBuysByUserAndPostStatusBeforeCursor(
                user.getId(), postStatus, cursorAt, cursorId, pageable);
        verify(groupBuyQueryMapper, times(2)).toWishListResponse(any(GroupBuy.class));

        assertThat(result.getPosts()).hasSize(1)
                .extracting(WishListResponse::getPostId)
                .containsExactly(2L);
        assertThat(result.getNextCursor()).isEqualTo(2L);
        assertThat(result.isHasMore()).isTrue();
    }

    @Test
    @DisplayName("관심 공구 리스트 조회 - 커서 존재 & 다음 페이지 없음")
    void getWishList_cursor_noNext() {
        int   limit      = 2;
        long  cursorId   = 2L;
        LocalDateTime at = LocalDateTime.of(2025, 6, 12, 13, 40);

        Pageable pageable = PageRequest.of(
                0, limit + 1,
                Sort.by("createdAt").descending().and(Sort.by("id").descending())
        );

        when(wishRepository.findGroupBuysByUserAndPostStatusBeforeCursor(
                user.getId(), postStatus, at, cursorId, pageable)
        ).thenReturn(groupBuys);
        when(groupBuyQueryMapper.toWishListResponse(groupBuy2)).thenReturn(res2);
        when(groupBuyQueryMapper.toWishListResponse(groupBuy1)).thenReturn(res1);

        PagedResponse<WishListResponse> result = getGroupBuyWishList.getGroupBuyWishList(
                user.getId(), postStatus, at, cursorId, limit);

        assertThat(result.getPosts()).hasSize(2);
        assertThat(result.getNextCursor()).isEqualTo(1L);
        assertThat(result.isHasMore()).isFalse();
    }

    @Test
    @DisplayName("관심 공구 리스트 조회 - 커서 없음 & 다음 페이지 존재")
    void getWishList_noCursor_hasNext() {
        int limit = 1;
        Pageable pageable = PageRequest.of(
                0, limit + 1,
                Sort.by("createdAt").descending().and(Sort.by("id").descending())
        );

        when(wishRepository.findGroupBuysByUserAndPostStatus(
                user.getId(), postStatus, pageable)
        ).thenReturn(groupBuys);
        when(groupBuyQueryMapper.toWishListResponse(groupBuy2)).thenReturn(res2);
        when(groupBuyQueryMapper.toWishListResponse(groupBuy1)).thenReturn(res1);

        PagedResponse<WishListResponse> result = getGroupBuyWishList.getGroupBuyWishList(
                user.getId(), postStatus, null, null, limit);

        assertThat(result.getPosts()).hasSize(1)
                .extracting(WishListResponse::getPostId)
                .containsExactly(2L);
        assertThat(result.getNextCursor()).isEqualTo(2L);
        assertThat(result.isHasMore()).isTrue();
    }

    @Test
    @DisplayName("관심 공구 리스트 조회 - 커서 없음 & 다음 페이지 없음")
    void getWishList_noCursor_noNext() {
        int limit = 3;
        Pageable pageable = PageRequest.of(
                0, limit + 1,
                Sort.by("createdAt").descending().and(Sort.by("id").descending())
        );

        when(wishRepository.findGroupBuysByUserAndPostStatus(
                user.getId(), postStatus, pageable)
        ).thenReturn(groupBuys);
        when(groupBuyQueryMapper.toWishListResponse(groupBuy2)).thenReturn(res2);
        when(groupBuyQueryMapper.toWishListResponse(groupBuy1)).thenReturn(res1);

        PagedResponse<WishListResponse> result = getGroupBuyWishList.getGroupBuyWishList(
                user.getId(), postStatus, null, null, limit);

        assertThat(result.getPosts()).hasSize(2);
        assertThat(result.getNextCursor()).isEqualTo(1L);
        assertThat(result.isHasMore()).isFalse();
    }
}
