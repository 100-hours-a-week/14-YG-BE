package com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyCommandService;

import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyInvalidStateException;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyNotFoundException;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyNotHostException;
import com.moogsan.moongsan_backend.domain.groupbuy.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

import static com.moogsan.moongsan_backend.domain.groupbuy.message.ResponseMessage.*;

@Service
@Transactional
@RequiredArgsConstructor
public class EndGroupBuy {

    private final GroupBuyRepository groupBuyRepository;
    private final Clock clock;

    /// 공구 게시글 공구 종료
    public void endGroupBuy(User currentUser, Long postId) {

        // 해당 공구가 존재하는지 조회 -> 없으면 404
        GroupBuy groupBuy = groupBuyRepository.findById(postId)
                .orElseThrow(GroupBuyNotFoundException::new);

        // 해당 공구가 OPEN인지 조회 -> 아니면 409
        if (groupBuy.getPostStatus().equals("CLOSED")) {
            throw new GroupBuyInvalidStateException(BEFORE_CLOSED);
        }

        // 해당 공구가 ENDED인지 조회 -> 맞으면 409
        if (groupBuy.getPostStatus().equals("ENDED")) {
            throw new GroupBuyInvalidStateException(AFTER_ENDED);
        }

        if (!groupBuy.isFixed()) {
            throw new GroupBuyInvalidStateException(BEFORE_FIXED);
        }

        // 해당 공구의 주최자가 해당 유저인지 조회 -> 아니면 403
        if(!groupBuy.getUser().getId().equals(currentUser.getId())) {
            throw new GroupBuyNotHostException(NOT_HOST);
        }

        //공구 게시글 status ENDED로 변경
        groupBuy.changePostStatus("ENDED");

        groupBuyRepository.save(groupBuy);

        // TODO V2, V3에서는 참여자 채팅방 해제 카운트 시작(2주- CS 고려), 익명 채팅방 즉시 해제

    }
}
