package com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyCommandService;

import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyInvalidStateException;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyNotFoundException;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyNotHostException;
import com.moogsan.moongsan_backend.domain.groupbuy.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.domain.order.repository.OrderRepository;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import static com.moogsan.moongsan_backend.domain.groupbuy.message.ResponseMessage.*;

@Service
@Transactional
@RequiredArgsConstructor
public class DeleteGroupBuy {

    private final GroupBuyRepository groupBuyRepository;
    private final OrderRepository orderRepository;
    private final Clock clock;

    /// 공구 게시글 삭제: 참여자가 아무도 없는, 주문 레코드가 없는 경우이므로 하드 삭제
    public void deleteGroupBuy(User currentUser, Long postId) {

        // 해당 공구가 존재하는지 조회 -> 아니면 404
        GroupBuy groupBuy = groupBuyRepository.findById(postId)
                .orElseThrow(GroupBuyNotFoundException::new);

        // 해당 공구의 status가 open인지 조회 -> 아니면 409
        if (!groupBuy.getPostStatus().equals("OPEN")
                || groupBuy.getDueDate().isBefore(LocalDateTime.now(clock))) {
            throw new GroupBuyInvalidStateException(NOT_OPEN);
        }

        // 해당 공구의 참여자가 1명 이상인지 조회 -> 아니면 409
        int participantCount = orderRepository.countByGroupBuyIdAndStatusNotIn(postId, List.of("CANCELED", "REFUNDED"));
        if(participantCount >= 1) {
            throw new GroupBuyInvalidStateException(EXIST_PARTICIPANT);
        }

        // 해당 공구의 주최자가 해당 유저인지 조회 -> 아니면 403
        if(!groupBuy.getUser().getId().equals(currentUser.getId())) {
            throw new GroupBuyNotHostException(NOT_HOST);
        }

        groupBuy.changePostStatus("DELETED");
        groupBuyRepository.save(groupBuy);

    }
}
