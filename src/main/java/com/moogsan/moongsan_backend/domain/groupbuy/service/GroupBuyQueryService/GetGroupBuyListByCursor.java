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
            Integer cursorSoldRatio,
            Integer cursorPrice,
            Integer limit,
            Boolean openOnly,
            String keyword
    ) {
        boolean isSearch = keyword != null && !keyword.isBlank();

        if (categoryId != null) {
            categoryRepository.findById(categoryId)
                    .orElseThrow(CategoryNotFoundException::new);
        }

        // 1) 정렬 기준
        Sort sort = isSearch
                ? Sort.by("id").descending()
                : switch (orderBy) {
            case "price_asc"    -> Sort.by("unitPrice").ascending()
                    .and(Sort.by("createdAt").ascending())
                    .and(Sort.by("id").ascending());
            case "ending_soon"  -> Sort.by("soldRatio").descending()
                    .and(Sort.by("id").ascending());
            case "due_soon_only"-> Sort.by("dueDate").ascending()
                    .and(Sort.by("id").ascending());
            default            -> Sort.by("createdAt").descending()
                    .and(Sort.by("id").descending());
        };

        // 2) 공통 스펙 (cursor 제외, 전체 count용)
        Specification<GroupBuy> baseSpec = Specification.where(excludeEndedOrDeleted())
                .and(dueSoonOnlyEq(orderBy))
                .and(categoryEq(categoryId))
                .and(openOnlyEq(openOnly))
                .and(keywordLike(keyword));

        // 3) 변하지 않는 전체 건수
        long totalCount = groupBuyRepository.count(baseSpec);

        // 4) 페이징 스펙에 cursor 포함
        Specification<GroupBuy> pagedSpec = baseSpec
                .and(cursorSpec(orderBy, cursorId, cursorCreatedAt, cursorSoldRatio, cursorPrice));

        // 5) limit+1 개 조회
        Pageable page = PageRequest.of(0, limit + 1, sort);
        Page<GroupBuy> result = groupBuyRepository.findAll(pagedSpec, page);
        List<GroupBuy> fetched = result.getContent();

        // 6) hasMore 판단 & 실제 반환할 entities
        boolean hasMore = fetched.size() > limit;
        List<GroupBuy> entities = hasMore
                ? fetched.subList(0, limit)
                : fetched;

        // 7) DTO 변환
        Map<Long, Boolean> wishMap = fetchWishUtil.fetchWishMap(userId, entities);
        List<BasicListResponse> posts = groupBuyQueryMapper.toBasicListWishResponses(entities, wishMap);

        // 8) nextCursor 계산 (entities 기준 마지막 요소)
        Long nextCursorId = null;
        Integer nextCursorPrice = null;
        LocalDateTime nextCreatedAt = null;
        Integer nextCursorSoldRatio = null;

        if (hasMore && !entities.isEmpty()) {
            GroupBuy last = entities.getLast();
            nextCursorId    = last.getId();
            nextCreatedAt   = last.getCreatedAt();
            if ("price_asc".equals(orderBy)) {
                nextCursorPrice = last.getUnitPrice();
            } else if ("ending_soon".equals(orderBy)) {
                nextCursorSoldRatio = last.getSoldRatio();
            }
        }

        // 9) 응답 빌드 (count는 변하지 않는 totalCount)
        return PagedResponse.<BasicListResponse>builder()
                .count(totalCount)
                .posts(posts)
                .nextCursor(nextCursorId != null ? nextCursorId.intValue() : null)
                .nextCursorPrice(nextCursorPrice)
                .nextSoldRatio(nextCursorSoldRatio)
                .nextCreatedAt(nextCreatedAt)
                .hasMore(hasMore)
                .build();
    }

    private Specification<GroupBuy> excludeEndedOrDeleted() {
        return (root, query, cb) ->
                cb.not(root.get("postStatus").in("ENDED", "DELETED"));
    }

    private Specification<GroupBuy> dueSoonOnlyEq(String orderBy) {
        return (root, query, cb) -> {
            Predicate statusOpen = cb.equal(root.get("postStatus"), "OPEN");
            return "due_soon_only".equals(orderBy)
                    ? cb.and(statusOpen, cb.isTrue(root.get("dueSoon")))
                    : cb.conjunction();
        };
    }

    private Specification<GroupBuy> categoryEq(Long categoryId) {
        return (root, query, cb) -> {
            if (categoryId == null) return cb.conjunction();
            return cb.equal(
                    root.join("groupBuyCategories")
                            .get("category")
                            .get("id"),
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
            if (keyword == null || keyword.isBlank()) return cb.conjunction();
            String p = "%" + keyword.trim().toLowerCase() + "%";
            Expression<String> title = cb.lower(root.get("title"));
            Expression<String> name  = cb.lower(root.get("name"));
            Expression<String> descr = cb.lower(root.get("description"));
            return cb.or(cb.like(title, p), cb.like(name, p), cb.like(descr, p));
        };
    }

    private Specification<GroupBuy> cursorSpec(
            String orderBy,
            Long cursorId,
            LocalDateTime cursorCreatedAt,
            Integer cursorSoldRatio,
            Integer cursorPrice
    ) {
        return (root, query, cb) -> {
            if (cursorId == null) return cb.conjunction();

            switch (orderBy) {
                case "price_asc": {
                    Path<Integer> price   = root.get("unitPrice");
                    Path<LocalDateTime> created = root.get("createdAt");
                    return cb.or(
                            // price가 더 큰 것
                            cb.greaterThan(price, cursorPrice),
                            // price 같고 createdAt이 더 나중인 것
                            cb.and(
                                    cb.equal(price, cursorPrice),
                                    cb.greaterThan(created, cursorCreatedAt)
                            ),
                            // price·createdAt 같고 id가 더 큰 것
                            cb.and(
                                    cb.equal(price, cursorPrice),
                                    cb.equal(created, cursorCreatedAt),
                                    cb.greaterThan(root.get("id"), cursorId)
                            )
                    );
                }
                case "ending_soon": {
                    Path<Integer> ratio = root.get("soldRatio");
                    Path<Long> id = root.get("id");
                    return cb.or(
                            cb.lessThan(ratio, cursorSoldRatio),
                            cb.and(cb.equal(ratio, cursorSoldRatio),
                                    cb.lessThan(root.get("id"), cursorId)),
                            cb.lessThan(id, cursorId)
                    );
                }
                case "due_soon_only": {
                    Path<LocalDateTime> due = root.get("dueDate");
                    return cb.or(
                            cb.lessThan(due, cursorCreatedAt),
                            cb.and(cb.equal(due, cursorCreatedAt),
                                    cb.lessThan(root.get("id"), cursorId))
                    );
                }
                default: {
                    Path<LocalDateTime> created = root.get("createdAt");
                    return cb.or(
                            cb.lessThan(created, cursorCreatedAt),
                            cb.and(cb.equal(created, cursorCreatedAt),
                                    cb.lessThan(root.get("id"), cursorId))
                    );
                }
            }
        };
    }
}
