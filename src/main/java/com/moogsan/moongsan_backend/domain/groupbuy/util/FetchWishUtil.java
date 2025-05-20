package com.moogsan.moongsan_backend.domain.groupbuy.util;

import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.groupbuy.mapper.GroupBuyQueryMapper;
import com.moogsan.moongsan_backend.domain.user.repository.WishRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FetchWishUtil {

    private final WishRepository wishRepository;

    public Map<Long, Boolean> fetchWishMap(Long userId, List<GroupBuy> posts) {
        if (userId == null || posts.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> ids = posts.stream()
                .map(GroupBuy::getId)
                .toList();
        Set<Long> wishedIds = new HashSet<>(wishRepository.findWishedGroupBuyIds(userId, ids));

        // Map<groupBuyId, isWished>
        return ids.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> wishedIds.contains(id)
                ));
    }

}
