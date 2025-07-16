package com.moogsan.moongsan_backend.groupbuy.application.service.query;

import com.moogsan.moongsan_backend.participantchat.domain.entity.ChatRoom;
import com.moogsan.moongsan_backend.participantchat.domain.repository.ChatRoomRepository;
import com.moogsan.moongsan_backend.groupbuy.presentation.dto.query.response.groupBuyList.HostedList.HostedListResponse;
import com.moogsan.moongsan_backend.groupbuy.presentation.dto.query.response.groupBuyList.PagedResponse;
import com.moogsan.moongsan_backend.groupbuy.domain.entity.GroupBuy;
import com.moogsan.moongsan_backend.groupbuy.application.mapper.GroupBuyQueryMapper;
import com.moogsan.moongsan_backend.groupbuy.domain.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.groupbuy.domain.util.FetchWishUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

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
            Long userId, String postStatus, Long cursorId, Integer limit) {

        // 공동구매 게시글 데이터 조회
        List<GroupBuy> groupBuys = fetchGroupBuys(userId, postStatus, cursorId, limit);

        // 찜 여부 매핑
        Map<Long, Boolean> wishMap = fetchWishUtil.fetchWishMap(userId, groupBuys);

        // 공동구매 게시글 아이디 추출
        List<Long> groupBuyIds = extractIds(groupBuys);

        // 공동구매 게시글, 채팅방 정보 매핑
        List<ChatRoom> chatRooms = chatRoomRepository.findByGroupBuy_IdInAndType(groupBuyIds, "PARTICIPANT");

        // DTO 변환
        List<HostedListResponse> posts = groupBuyQueryMapper.toHostedListWishResponses(groupBuys, wishMap, chatRooms);

        // 더보기 여부 확인
        boolean hasMore = posts.size() > limit;

        // 실제 데이터 크기로 조정
        List<HostedListResponse> hostedGroupBuys = posts.size() > limit ? posts.subList(0, limit) : posts;

        // 다음 커서 지정
        Long nextCursor = hostedGroupBuys.isEmpty() ? null : hostedGroupBuys.getLast().getPostId();

        return PagedResponse.<HostedListResponse>builder()
                .count(hostedGroupBuys.size())
                .posts(hostedGroupBuys)
                .nextCursor(nextCursor != null ? nextCursor.intValue() : null)
                .hasMore(hasMore)
                .build();
    }

    private List<GroupBuy> fetchGroupBuys(Long userId, String postStatus, Long cursorId, Integer limit) {
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

        return groupBuys;
    }

    private List<Long> extractIds(List<GroupBuy> groupBuys){
        return groupBuys.stream()
                .map(GroupBuy::getId)
                .toList();
    }
}
