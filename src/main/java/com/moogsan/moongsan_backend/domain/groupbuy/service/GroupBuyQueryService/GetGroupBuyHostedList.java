package com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyQueryService;

import com.moogsan.moongsan_backend.domain.chatting.entity.ChatRoom;
import com.moogsan.moongsan_backend.domain.chatting.repository.ChatRoomRepository;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.HostedList.HostedListResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.PagedResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.groupbuy.mapper.GroupBuyQueryMapper;
import com.moogsan.moongsan_backend.domain.groupbuy.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.domain.groupbuy.util.FetchWishUtil;
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
    private final ChatRoomRepository chatRoomRepository;
    private final GroupBuyQueryMapper groupBuyQueryMapper;
    private final FetchWishUtil fetchWishUtil;

    /// 주최 공구 리스트 조회
    public PagedResponse<HostedListResponse> getGroupBuyHostedList(
            Long userId,
            String postStatus,
            Long cursorId,
            Integer limit) {

        String status = postStatus.toUpperCase();

        Pageable page = PageRequest.of(0, limit + 1, Sort.by("id").descending());

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

        Map<Long, Boolean> wishMap = fetchWishUtil.fetchWishMap(userId, groupBuys);

        List<Long> groupBuyIds = groupBuys.stream()
                .map(GroupBuy::getId)
                .collect(Collectors.toList());


        List<ChatRoom> chatRooms = chatRoomRepository.findByGroupBuy_IdInAndType(
                groupBuyIds,
                "PARTICIPANT"
        );

        List<HostedListResponse> posts = groupBuyQueryMapper
                .toHostedListWishResponses(groupBuys, wishMap, chatRooms);

        List<HostedListResponse> hostedGroupBuys = posts.size() > limit
                ? posts.subList(0, limit)
                : posts;

        // 다음 커서 및 더보기 여부
        Long nextCursor = hostedGroupBuys.isEmpty()
                ? null
                : hostedGroupBuys.getLast().getPostId();
        boolean hasMore = posts.size() > limit;

        return PagedResponse.<HostedListResponse>builder()
                .count(hostedGroupBuys.size())
                .posts(hostedGroupBuys)
                .nextCursor(nextCursor != null ? nextCursor.intValue() : null)
                .hasMore(hasMore)
                .build();
    }
}
