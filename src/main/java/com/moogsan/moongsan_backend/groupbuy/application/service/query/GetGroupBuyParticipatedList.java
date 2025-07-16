package com.moogsan.moongsan_backend.groupbuy.application.service.query;

import com.moogsan.moongsan_backend.participantchat.domain.entity.ChatRoom;
import com.moogsan.moongsan_backend.participantchat.domain.repository.ChatRoomRepository;
import com.moogsan.moongsan_backend.groupbuy.presentation.dto.query.response.groupBuyList.PagedResponse;
import com.moogsan.moongsan_backend.groupbuy.presentation.dto.query.response.groupBuyList.ParticipatedList.ParticipatedListResponse;
import com.moogsan.moongsan_backend.groupbuy.domain.entity.GroupBuy;
import com.moogsan.moongsan_backend.groupbuy.application.mapper.GroupBuyQueryMapper;
import com.moogsan.moongsan_backend.groupbuy.domain.util.FetchWishUtil;
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
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly=true)
@RequiredArgsConstructor
public class GetGroupBuyParticipatedList {

    private final OrderRepository orderRepository;
    private final ChatRoomRepository chatRoomRepository;
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
        // 주문 데이터 조회
        List<Order> orders = fetchOrders(userId, sort, cursorCreatedAt, cursorId, limit);

        // 공동구매 게시글 추출
        List<GroupBuy> groupBuys = extractGroupBuys(orders);

        // 찜 여부 매핑
        Map<Long, Boolean> wishMap = fetchWishUtil.fetchWishMap(userId, groupBuys);

        // 참여자 채팅방 매핑
        List<ChatRoom> chatRooms = fetchChatRooms(groupBuys);

        // DTO 매핑
        List<ParticipatedListResponse> posts = groupBuyQueryMapper.toParticipatedListWishResponse(orders, wishMap, chatRooms);

        // 더보기 여부 확인
        boolean hasMore = posts.size() > limit;

        // 실제 데이터 크기로 조정
        List<ParticipatedListResponse> participatedGroupBuys = posts.size() > limit ? posts.subList(0, limit) : posts;

        // 다음 커서 지정
        Long nextCursor = participatedGroupBuys.isEmpty() ? null : participatedGroupBuys.getLast().getPostId();

        return PagedResponse.<ParticipatedListResponse>builder()
                .count(participatedGroupBuys.size())
                .posts(participatedGroupBuys)
                .nextCursor(nextCursor != null ? nextCursor.intValue() : null)
                .hasMore(hasMore)
                .build();
    }

    private List<Order> fetchOrders(
            Long userId, String sort, LocalDateTime cursorCreatedAt, Long cursorId, Integer limit) {
        String status = sort.toUpperCase();

        Pageable page = PageRequest.of(
                0,
                limit + 1,
                Sort.by("createdAt").descending()
                        .and(Sort.by("id").descending())
        );

        // cursorId가 없으면 cursor 조건 제외
        List<Order> orders;
        if (cursorCreatedAt == null) {
            orders = orderRepository.findByUserAndPostStatusAndNotRefunded(
                    userId,
                    status,
                    page
            );
        } else {
            orders = orderRepository.findByUserAndPostStatusAndNotRefundedBeforeCursor(
                    userId,
                    status,
                    cursorCreatedAt,
                    cursorId,
                    page
            );
        }

        return orders;
    }

    private List<GroupBuy> extractGroupBuys(List<Order> orders){
        return orders.stream()
                .map(Order::getGroupBuy)
                .toList();
    }

    private List<ChatRoom> fetchChatRooms(List<GroupBuy> groupBuys) {
        List<Long> groupBuyIds = groupBuys.stream()
                .map(GroupBuy::getId)
                .collect(Collectors.toList());

        return chatRoomRepository.findByGroupBuy_IdInAndType(
                groupBuyIds,
                "PARTICIPANT"
        );
    }
}
