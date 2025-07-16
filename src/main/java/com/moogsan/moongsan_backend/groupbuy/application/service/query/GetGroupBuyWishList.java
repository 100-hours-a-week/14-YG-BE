package com.moogsan.moongsan_backend.groupbuy.application.service.query;

import com.moogsan.moongsan_backend.groupbuy.presentation.dto.query.response.groupBuyList.PagedResponse;
import com.moogsan.moongsan_backend.groupbuy.presentation.dto.query.response.groupBuyList.WishList.WishListResponse;
import com.moogsan.moongsan_backend.groupbuy.domain.entity.GroupBuy;
import com.moogsan.moongsan_backend.groupbuy.application.mapper.GroupBuyQueryMapper;
import com.moogsan.moongsan_backend.domain.user.repository.WishRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly=true)
@RequiredArgsConstructor
public class GetGroupBuyWishList {

    private final GroupBuyQueryMapper groupBuyQueryMapper;
    private final WishRepository wishRepository;

    /// 관심 공구 리스트 조회: 관심 등록 순으로 커서 적용 필요
    public PagedResponse<WishListResponse> getGroupBuyWishList(
            Long userId, String postStatus, LocalDateTime cursorCreatedAt, Long cursorId, Integer limit) {

        // 공동구매 게시글 데이터 조회
        List<GroupBuy> groupBuys = fetchGroupBuys(userId, postStatus, cursorCreatedAt, cursorId, limit);

        // DTO 변환
        List<WishListResponse> posts = groupBuys.stream()
                .map(groupBuyQueryMapper::toWishListResponse)
                .toList();

        // 더보기 여부 확인
        boolean hasMore = posts.size() > limit;

        // 실제 데이터 크기로 조정
        List<WishListResponse> wishedGroupBuys = posts.size() > limit ? posts.subList(0, limit) : posts;

        // 다음 커서 지정
        Long nextCursor = wishedGroupBuys.isEmpty() ? null : wishedGroupBuys.getLast().getPostId();

        return PagedResponse.<WishListResponse>builder()
                .count(wishedGroupBuys.size())
                .posts(wishedGroupBuys)
                .nextCursor(nextCursor != null ? nextCursor.intValue() : null)
                .hasMore(hasMore)
                .build();
    }

    private List<GroupBuy> fetchGroupBuys(
            Long userId, String postStatus, LocalDateTime cursorCreatedAt, Long cursorId, Integer limit) {
        String status = postStatus.toUpperCase();

        Pageable page = PageRequest.of(
                0,
                limit + 1,
                Sort.by("createdAt").descending()
                        .and(Sort.by("id").descending())
        );

        // cursorId가 없으면 cursor 조건 제외
        List<GroupBuy> groupBuys;
        if (cursorId == null) {
            groupBuys = wishRepository
                    .findGroupBuysByUserAndPostStatus(
                            userId,
                            status,
                            page
                    );
        } else {
            groupBuys = wishRepository
                    .findGroupBuysByUserAndPostStatusBeforeCursor(
                            userId,
                            status,
                            cursorCreatedAt,
                            cursorId,
                            page
                    );
        }

        return groupBuys;
    }
}
