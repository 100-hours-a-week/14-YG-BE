package com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyQueryService;

import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.BasicList.BasicListResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.PagedResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.entity.Category;
import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.CategoryNotFoundException;
import com.moogsan.moongsan_backend.domain.groupbuy.mapper.GroupBuyQueryMapper;
import com.moogsan.moongsan_backend.domain.groupbuy.repository.CategoryRepository;
import com.moogsan.moongsan_backend.domain.groupbuy.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.domain.groupbuy.util.FetchWishUtil;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
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
    private final CategoryRepository categoryRepository;
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

        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(CategoryNotFoundException::new);

        }


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
        Specification<GroupBuy> spec = Specification
                .where(distinct())
                .and(excludeEndedOrDeleted())
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
                .count(result.getTotalElements())
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

    private Specification<GroupBuy> distinct() {
        return (root, query, cb) -> {
            query.distinct(true);
            return cb.conjunction();   // where 조건 없음
        };
    }

    private Specification<GroupBuy> excludeEndedOrDeleted() {
        return (root, query, cb) ->
                cb.not(root.get("postStatus").in("ENDED", "DELETED"));
    }


    private Specification<GroupBuy> dueSoonOnlyEq(String orderBy) {
        return (root, query, cb) -> {
            Predicate statusOpen = cb.equal(root.get("postStatus"), "OPEN");
            if (!"due_soon_only".equals(orderBy)) {
                return cb.conjunction();
            }
            return cb.and(
                    statusOpen,
                    cb.isTrue(root.get("dueSoon"))
            );
        };
    }

    private Specification<GroupBuy> categoryEq(Long categoryId) {
        return (root, query, cb) -> {
            if (categoryId == null) {
                return cb.conjunction();
            }
            // 1) root: GroupBuy
            // 2) join("groupBuyCategories"): GroupBuy ↔ GroupBuyCategory (List<GroupBuyCategory>)
            // 3) get("category"): GroupBuyCategory 안의 Category 엔티티
            // 4) get("id"): Category 엔티티의 PK (category_id)
            return cb.equal(
                    root.join("groupBuyCategories")   // 엔티티 필드명: groupBuyCategories
                            .get("category")              // GroupBuyCategory 엔티티의 category 필드
                            .get("id"),                   // Category 엔티티의 id 필드
                    categoryId
            );
        };
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
                            cb.greaterThan(price, cursorPrice),
                            cb.and(
                                    cb.equal(price, cursorPrice),
                                    cb.greaterThan(created, cursorCreatedAt)
                            ),
                            cb.and(
                                    cb.equal(price, cursorPrice),
                                    cb.equal(created, cursorCreatedAt),
                                    cb.greaterThan(root.get("id"), cursorId)
                            )
                    );
                }
                case "ending_soon":
                case "due_soon_only": {
                    Path<LocalDateTime> dueDate = root.get("dueDate");
                    return cb.or(
                            cb.greaterThan(dueDate, cursorCreatedAt),
                            cb.and(
                                    cb.equal(dueDate, cursorCreatedAt),
                                    cb.greaterThan(root.get("id"), cursorId)
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
