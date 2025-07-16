package com.moogsan.moongsan_backend.groupbuy.application.service.command;

import com.moogsan.moongsan_backend.groupbuy.domain.entity.GroupBuy;
import com.moogsan.moongsan_backend.groupbuy.domain.exception.specific.GroupBuyInvalidStateException;
import com.moogsan.moongsan_backend.groupbuy.domain.exception.specific.GroupBuyNotFoundException;
import com.moogsan.moongsan_backend.groupbuy.domain.exception.specific.GroupBuyNotHostException;
import com.moogsan.moongsan_backend.groupbuy.domain.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import com.moogsan.moongsan_backend.groupbuy.domain.service.GroupBuyEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import static com.moogsan.moongsan_backend.groupbuy.domain.message.ResponseMessage.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class EndGroupBuy {

    private final GroupBuyRepository groupBuyRepository;
    private final GroupBuyEventService groupBuyEventService;
    private final Clock clock;

    /// 공구 게시글 공구 종료
    public void endGroupBuy(User currentUser, Long postId) {

        GroupBuy groupBuy = fetchAndValidate(currentUser.getId(), postId);

        // 공동구매 게시글 상태 전환
        groupBuy.changePostStatus("ENDED");
        groupBuyRepository.save(groupBuy);

        // 공구 상태 업데이트 이벤트 발행
        groupBuyEventService.publishGroupBuyUpdated(groupBuy);

        // 공구 종료 이벤트 발행
        groupBuyEventService.publishGroupBuyEnded(groupBuy);


        // 참여자 채팅방 해제 카운트 시작(2주- CS 고려), 익명 채팅방 즉시 해제

    }

    private GroupBuy fetchAndValidate(Long userId, Long postId) {
        // 해당 공구가 존재하는지 조회 -> 없으면 404
        GroupBuy groupBuy = groupBuyRepository.findById(postId)
                .orElseThrow(GroupBuyNotFoundException::new);

        // 해당 공구가 OPEN인지 조회 -> 아니면 409
        if (groupBuy.getPostStatus().equals("OPEN")) {
            throw new GroupBuyInvalidStateException(BEFORE_CLOSED);
        }

        // 해당 공구가 ENDED인지 조회 -> 맞으면 409
        if (groupBuy.getPostStatus().equals("ENDED")) {
            throw new GroupBuyInvalidStateException(AFTER_ENDED);
        }

        if (!groupBuy.isFinalized()) {
            throw new GroupBuyInvalidStateException(BEFORE_FIXED);
        }

        // 해당 공구의 주최자가 해당 유저인지 조회 -> 아니면 403
        if(!groupBuy.getUser().getId().equals(userId)) {
            throw new GroupBuyNotHostException(NOT_HOST);
        }

        return groupBuy;
    }
}
