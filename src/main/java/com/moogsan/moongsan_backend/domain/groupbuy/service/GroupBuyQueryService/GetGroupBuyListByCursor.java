package com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyQueryService;

import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.BasicList.BasicListResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.PagedResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.groupbuy.mapper.GroupBuyQueryMapper;
import com.moogsan.moongsan_backend.domain.groupbuy.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.domain.groupbuy.util.FetchWishUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
public class GetGroupBuyListByCursor {

    private final GroupBuyRepository groupBuyRepository;
    private final GroupBuyQueryMapper groupBuyQueryMapper;
    private final FetchWishUtil fetchWishUtil;

    /// 공구 리스트 조회
    public PagedResponse<BasicListResponse> getGroupBuyListByCursor(
            Long userId,
            Long categoryId,
            String orderBy,        // e.g. "latest", "ending_soon", "price_asc", "due_soon_only"
            Long cursorId,
            LocalDateTime cursorCreatedAt,
            Integer cursorPrice,
            Integer limit
    ) {
        // 페이징 객체 생성 (첫 페이지는 0번 인덱스, limit 만큼)
        Pageable page = PageRequest.of(0, limit);

        // 각 정렬에 따라 cursor 유무 분기
        List<GroupBuy> entities;
        switch (orderBy) {
            case "due_soon_only":
                entities = groupBuyRepository.findDueSoonOnly(page);

                break;

            case "price_asc":
                int lastPrice = (cursorPrice != null) ? cursorPrice : 0;
                LocalDateTime lastCreatedForPrice = (cursorCreatedAt != null)
                        ? cursorCreatedAt
                        : LocalDateTime.now();

                if (cursorId == null) {
                    // 첫 페이지: 가격 오름차순
                    if (categoryId != null) {
                        entities = groupBuyRepository.findByCategoryPriceOrder(
                                categoryId, page);
                    } else {
                        entities = groupBuyRepository.findAllPriceOrder(
                                page);
                    }
                } else {
                    // 다음 페이지: 가격 오름차순 커서
                    if (categoryId != null) {
                        entities = groupBuyRepository.findByCategoryPriceAscCursor(
                                categoryId, lastPrice, lastCreatedForPrice, cursorId, page);
                    } else {
                        entities = groupBuyRepository.findByPriceAscCursor(
                                lastPrice, lastCreatedForPrice, cursorId, page);
                    }
                }
                break;

            case "ending_soon":
                LocalDateTime lastCreatedForDue = (cursorCreatedAt != null)
                        ? cursorCreatedAt
                        : LocalDateTime.now();

                if (cursorId == null) {
                    // 첫 페이지: 마감 임박순
                    if (categoryId != null) {
                        entities = groupBuyRepository.findByCategoryDueSoonOrder(
                                categoryId, page);
                    } else {
                        entities = groupBuyRepository.findEndingSoon(page);
                    }
                } else {
                    // 다음 페이지: 마감 임박순 커서
                    if (categoryId != null) {
                        entities = groupBuyRepository.findByCategoryEndingSoonCursor(
                                categoryId, lastCreatedForDue, cursorId, page);
                    } else {
                        entities = groupBuyRepository.findByEndingSoonCursor(
                                lastCreatedForDue, cursorId, page);
                    }
                }
                break;

            default:  // "latest"
                if (cursorId == null) {
                    // 첫 페이지: 최신순
                    if (categoryId != null) {
                        entities = groupBuyRepository.findByCategoryCreatedOrder(
                                categoryId, page);
                    } else {
                        entities = groupBuyRepository.findAllCreatedOrder(
                                page);
                    }
                } else {
                    // 다음 페이지: 최신순 커서
                    if (categoryId != null) {
                        entities = groupBuyRepository.findByCategoryCreatedCursor(
                                categoryId, cursorId, page);
                    } else {
                        entities = groupBuyRepository.findByCreatedCursor(
                                cursorId, page);
                    }
                }
                break;
        }


        // DTO 매핑
        Map<Long, Boolean> wishMap = fetchWishUtil.fetchWishMap(userId, entities);

        // 4) DTO 매핑
        List<BasicListResponse> posts = groupBuyQueryMapper.toBasicListWishResponses(entities, wishMap);

        // 다음 커서 & hasMore 계산
        boolean hasMore = posts.size() == limit;

        Long nextCursor      = null;
        Integer nextCursorPrice = null;
        LocalDateTime nextCreatedAt = null;

        if (hasMore) {
            BasicListResponse last = posts.getLast();
            nextCursor    = last.getPostId();
            nextCreatedAt = last.getCreatedAt();
            if ("price_asc".equals(orderBy)) {
                nextCursorPrice = last.getUnitPrice();
            }
        }

        return PagedResponse.<BasicListResponse>builder()
                .count(posts.size())
                .posts(posts)
                .nextCursor(nextCursor != null ? nextCursor.intValue() : null)  // int로 변환
                .nextCursorPrice(nextCursorPrice)
                .nextCreatedAt(nextCreatedAt)
                .hasMore(hasMore)
                .build();
    }
}
