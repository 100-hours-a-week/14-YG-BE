package com.moogsan.moongsan_backend.domain.groupbuy.facade.command;

import com.moogsan.moongsan_backend.domain.groupbuy.dto.command.request.*;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.command.response.DescriptionDto;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import reactor.core.publisher.Mono;

public interface GroupBuyCommandFacade {

    Long createGroupBuy(User user, CreateGroupBuyRequest request);

    void updateGroupBuy(User user, UpdateGroupBuyRequest request, Long postId);

    void deleteGroupBuy(User user, Long postId);

    void leaveGroupBuy(User user, Long postId);

    void endGroupBuy(User user, Long postId);

    Mono<DescriptionDto> generateDescription(String url, String sessionId); // ✨ 추가됨
}
