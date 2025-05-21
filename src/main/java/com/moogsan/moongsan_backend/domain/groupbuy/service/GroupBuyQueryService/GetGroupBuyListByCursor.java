package com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyQueryService;

import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.BasicList.BasicListResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.PagedResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.groupbuy.mapper.GroupBuyQueryMapper;
import com.moogsan.moongsan_backend.domain.groupbuy.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.domain.groupbuy.util.FetchWishUtil;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional(readOnly=true)
@RequiredArgsConstructor
public class GetGroupBuyListByCursor {

    private final GroupBuyRepository groupBuyRepository;
    private final FetchWishUtil fetchWishUtil;
    private final GroupBuyQueryMapper groupBuyQueryMapper;

    public PagedResponse<BasicListResponse> getGroupBuyListByCursor(
            Long userId,
            Long categoryId,
            String orderBy,
            Long cursorId,
            LocalDateTime cursorCreatedAt,
            Integer cursorPrice,
            Integer limit,
            Boolean openOnly,
            String keyword
    ) {
        boolean isSearch = keyword != null && !keyword.isBlank();

        // 1) 정렬 기준
        Sort sort = isSearch
                ? Sort.by("id").descending()
                : switch (orderBy) {
                    case "price_asc"    -> Sort.by("unitPrice").ascending()
                            .and(Sort.by("createdAt").ascending())
                            .and(Sort.by("id").ascending());
                    case "ending_soon"  -> Sort.by("dueDate").ascending()
                            .and(Sort.by("createdAt").descending())
                            .and(Sort.by("id").descending());
                    case "due_soon_only"-> Sort.by("dueDate").ascending()
                            .and(Sort.by("id").ascending());
                    default            -> Sort.by("createdAt").descending()
                            .and(Sort.by("id").descending());
        };

        // 2) 페이징 객체
        Pageable page = PageRequest.of(0, limit, sort);

        // 3) Specification 조립
        Specification<GroupBuy> spec = Specification.where(excludeEndedOrDeleted())
                .and(dueSoonOnlyEq(orderBy))
                .and(categoryEq(categoryId))
                .and(openOnlyEq(openOnly))
                .and(keywordLike(keyword))
                .and(cursorSpec(orderBy, cursorId, cursorCreatedAt, cursorPrice));

        // 4) DB 조회 (필터+정렬+커서+페이징)
        Page<GroupBuy> result = groupBuyRepository.findAll(spec, page);
        List<GroupBuy> entities = result.getContent();

        // 5) 찜 여부 맵 & DTO 변환
        Map<Long, Boolean> wishMap = fetchWishUtil.fetchWishMap(userId, entities);
        List<BasicListResponse> posts = groupBuyQueryMapper.toBasicListWishResponses(entities, wishMap);

        // 6) 다음 커서 계산
        boolean hasMore = result.hasNext();
        Long nextCursorId = null;
        Integer nextCursorPrice = null;
        LocalDateTime nextCreatedAt = null;

        if (hasMore) {
            GroupBuy last = entities.getLast();
            nextCursorId    = last.getId();
            nextCreatedAt   = last.getCreatedAt();
            if ("price_asc".equals(orderBy)) {
                nextCursorPrice = last.getUnitPrice();
            }
        }

        return PagedResponse.<BasicListResponse>builder()
                .count(posts.size())
                .posts(posts)
                .nextCursor(nextCursorId != null ? nextCursorId.intValue() : null)
                .nextCursorPrice(nextCursorPrice)
                .nextCreatedAt(nextCreatedAt)
                .hasMore(hasMore)
                .build();
    }

    // ────────────────────────────────────────────────────────────────────
    // Specification 헬퍼 메서드들
    // ────────────────────────────────────────────────────────────────────

    private Specification<GroupBuy> excludeEndedOrDeleted() {
        return (root, query, cb) ->
                cb.not(root.get("postStatus").in("ENDED", "DELETED"));
    }

    private Specification<GroupBuy> dueSoonOnlyEq(String orderBy) {
        return (root, query, cb) -> {
            if (!"due_soon_only".equals(orderBy)) {
                return cb.conjunction();
            }
            return cb.isTrue(root.get("dueSoon"));
        };
    }

    private Specification<GroupBuy> categoryEq(Long categoryId) {
        return (root, query, cb) ->
                categoryId == null
                        ? cb.conjunction()
                        : cb.equal(root.get("category").get("id"), categoryId);
    }

    private Specification<GroupBuy> openOnlyEq(Boolean openOnly) {
        return (root, query, cb) ->
                Boolean.TRUE.equals(openOnly)
                        ? cb.equal(root.get("postStatus"), "OPEN")
                        : cb.conjunction();
    }

    private Specification<GroupBuy> keywordLike(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            Expression<String> title   = cb.lower(root.get("title"));
            Expression<String> name    = cb.lower(root.get("name"));
            Expression<String> descr   = cb.lower(root.get("description"));

            return cb.or(
                    cb.like(title,   pattern),
                    cb.like(name,    pattern),
                    cb.like(descr,   pattern)
            );
        };
    }

    private Specification<GroupBuy> cursorSpec(
            String orderBy,
            Long cursorId,
            LocalDateTime cursorCreatedAt,
            Integer cursorPrice
    ) {
        return (root, query, cb) -> {
            if (cursorId == null) {
                return cb.conjunction();
            }
            // 각 페이징 전략별 커서 조건
            switch (orderBy) {
                case "price_asc": {
                    Path<Integer> price  = root.get("unitPrice");
                    Path<LocalDateTime> created = root.get("createdAt");
                    return cb.or(
                            cb.lessThan(price, cursorPrice),
                            cb.and(
                                    cb.equal(price, cursorPrice),
                                    cb.lessThan(created, cursorCreatedAt)
                            ),
                            cb.and(
                                    cb.equal(price, cursorPrice),
                                    cb.equal(created, cursorCreatedAt),
                                    cb.lessThan(root.get("id"), cursorId)
                            )
                    );
                }
                case "ending_soon":
                case "due_soon_only": {
                    Path<LocalDateTime> dueDate = root.get("dueDate");
                    return cb.or(
                            cb.lessThan(dueDate, cursorCreatedAt),
                            cb.and(
                                    cb.equal(dueDate, cursorCreatedAt),
                                    cb.lessThan(root.get("id"), cursorId)
                            )
                    );
                }
                default: { // latest
                    Path<LocalDateTime> createdAt = root.get("createdAt");
                    return cb.or(
                            cb.lessThan(createdAt, cursorCreatedAt),
                            cb.and(
                                    cb.equal(createdAt, cursorCreatedAt),
                                    cb.lessThan(root.get("id"), cursorId)
                            )
                    );
                }
            }
        };
    }
}
