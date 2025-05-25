package com.moogsan.moongsan_backend.domain.groupbuy.facade;

import com.moogsan.moongsan_backend.domain.groupbuy.dto.command.request.*;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.command.response.DescriptionDto;
import com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyCommandService.*;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GroupBuyCommandFacadeImpl implements GroupBuyCommandFacade {

    private final CreateGroupBuy createGroupBuy;
    private final UpdateGroupBuy updateGroupBuy;
    private final DeleteGroupBuy deleteGroupBuy;
    private final LeaveGroupBuy leaveGroupBuy;
    private final EndGroupBuy endGroupBuy;
    private final GenerateDescription generateDescription;

    @Override
    public Long createGroupBuy(User user, CreateGroupBuyRequest request) {
        return createGroupBuy.createGroupBuy(user, request);
    }

    @Override
    public void updateGroupBuy(User user, UpdateGroupBuyRequest request, Long postId) {
        updateGroupBuy.updateGroupBuy(user, request, postId);
    }

    @Override
    public void deleteGroupBuy(User user, Long postId) {
        deleteGroupBuy.deleteGroupBuy(user, postId);
    }

    @Override
    public void leaveGroupBuy(User user, Long postId) {
        leaveGroupBuy.leaveGroupBuy(user, postId);
    }

    @Override
    public void endGroupBuy(User user, Long postId) {
        endGroupBuy.endGroupBuy(user, postId);
    }

    @Override
    public Mono<DescriptionDto> generateDescription(String url, String sessionId) {
        return generateDescription.generateDescription(url, sessionId);
    }
}
