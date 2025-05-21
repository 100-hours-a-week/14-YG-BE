package com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyQueryService;

import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.PagedResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.ParticipatedList.ParticipatedListResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.groupbuy.mapper.GroupBuyQueryMapper;
import com.moogsan.moongsan_backend.domain.groupbuy.util.FetchWishUtil;
import com.moogsan.moongsan_backend.domain.order.entity.Order;
import com.moogsan.moongsan_backend.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional(readOnly=true)
@RequiredArgsConstructor
public class GetGroupBuyParticipatedList {

    private final OrderRepository orderRepository;
    private final GroupBuyQueryMapper groupBuyQueryMapper;
    private final FetchWishUtil fetchWishUtil;

    /// 참여 공구 리스트 조회: 주문 생성 순으로 커서 적용 추가 필요
    public PagedResponse<ParticipatedListResponse> getGroupBuyParticipatedList(
            Long userId,
            String sort,
            LocalDateTime cursorCreatedAt,
            Long cursorId,
            Integer limit
    ) {
        String status = sort.toUpperCase();

        Pageable page = PageRequest.of(
                0,
                limit,
                Sort.by("createdAt").descending()
                        .and(Sort.by("id").descending())
        );

        // cursorId가 없으면 cursor 조건 제외
        List<Order> orders;
        if (cursorCreatedAt == null) {
            orders = orderRepository.findByUserAndPostStatusAndNotCanceled(
                    userId,
                    status,
                    page
            );
        } else {
            orders = orderRepository.findByUserAndPostStatusAndNotCanceledBeforeCursor(
                    userId,
                    status,
                    cursorCreatedAt,
                    cursorId,
                    page
            );
        }

        // 매핑
        List<GroupBuy> groupBuys = orders.stream()
                .map(Order::getGroupBuy)
                .toList();
        Map<Long, Boolean> wishMap = fetchWishUtil.fetchWishMap(userId, groupBuys);

        // DTO 매핑
        List<ParticipatedListResponse> posts = groupBuyQueryMapper.toParticipatedListWishResponse(orders, wishMap);

        // 다음 커서 및 더보기 여부
        Long nextCursor = posts.isEmpty()
                ? null
                : posts.getLast().getPostId();
        boolean hasMore = posts.size() == limit;

        return PagedResponse.<ParticipatedListResponse>builder()
                .count(posts.size())
                .posts(posts)
                .nextCursor(nextCursor != null ? nextCursor.intValue() : null)
                .hasMore(hasMore)
                .build();
    }

}
