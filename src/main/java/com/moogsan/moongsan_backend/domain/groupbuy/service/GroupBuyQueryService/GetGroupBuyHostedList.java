package com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyQueryService;

import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.HostedList.HostedListResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.PagedResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.groupbuy.mapper.GroupBuyQueryMapper;
import com.moogsan.moongsan_backend.domain.groupbuy.repository.GroupBuyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly=true)
@RequiredArgsConstructor
public class GetGroupBuyHostedList {

    private final GroupBuyRepository groupBuyRepository;
    private final GroupBuyQueryMapper groupBuyQueryMapper;

    /// 주최 공구 리스트 조회
    public PagedResponse<HostedListResponse> getGroupBuyHostedList(
            Long userId,
            String postStatus,
            Long cursorId,
            Integer limit) {

        String status = postStatus.toUpperCase();

        Pageable page = PageRequest.of(0, limit, Sort.by("id").descending());

        // cursorId가 없으면 cursor 조건 제외
        List<GroupBuy> groupBuys;
        if (cursorId == null) {
            groupBuys = groupBuyRepository.findByUser_IdAndPostStatus (
                    userId,
                    status,
                    page
            );
        } else {
            groupBuys = groupBuyRepository.findByUser_IdAndPostStatusAndIdLessThan (
                    userId,
                    status,
                    cursorId,
                    page
            );
        }

        Map<Long, Boolean> wishMap = fetchWishMap(userId, groupBuys);

        List<HostedListResponse> posts = groupBuys.stream()
                .map(gb -> {
                    // Map에서 위시 여부 꺼내기 (없으면 false)
                    boolean isWished = wishMap.getOrDefault(gb.getId(), false);

                    // DTO 변환
                    return groupBuyQueryMapper.toHostedListResponse(
                            gb,
                            isWished
                    );
                })
                .collect(Collectors.toList());

        // 다음 커서 및 더보기 여부
        Long nextCursor = posts.isEmpty()
                ? null
                : posts.getLast().getPostId();
        boolean hasMore = posts.size() == limit;

        return PagedResponse.<HostedListResponse>builder()
                .count(posts.size())
                .posts(posts)
                .nextCursor(nextCursor != null ? nextCursor.intValue() : null)
                .hasMore(hasMore)
                .build();
    }
}
