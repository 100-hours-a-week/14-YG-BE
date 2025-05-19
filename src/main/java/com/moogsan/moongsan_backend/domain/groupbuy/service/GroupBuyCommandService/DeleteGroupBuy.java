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

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class DeleteGroupBuy {

    private final GroupBuyRepository groupBuyRepository;
    private final OrderRepository orderRepository;

    /// 공구 게시글 삭제: 참여자가 아무도 없는, 주문 레코드가 없는 경우이므로 하드 삭제
    // TODO V2
    public void deleteGroupBuy(User currentUser, Long postId) {

        // 해당 공구가 존재하는지 조회 -> 아니면 404
        GroupBuy groupBuy = groupBuyRepository.findById(postId)
                .orElseThrow(GroupBuyNotFoundException::new);

        // 해당 공구의 status가 open인지 조회 -> 아니면 409
        if (!groupBuy.getPostStatus().equals("OPEN")
                || groupBuy.getDueDate().isBefore(LocalDateTime.now())) {
            throw new GroupBuyInvalidStateException("공구 삭제는 공구가 열려있는 상태에서만 가능합니다.");
        }

        // 해당 공구의 참여자가 0명인지 조회 -> 아니면 409
        int participantCount = orderRepository.countByGroupBuyId(postId);
        if(participantCount != 0) {
            throw new GroupBuyInvalidStateException("참여자가 1명 이상일 경우 공구를 삭제할 수 없습니다.");
        }

        // 해당 공구의 주최자가 해당 유저인지 조회 -> 아니면 403
        if(!groupBuy.getUser().getId().equals(currentUser.getId())) {
            throw new GroupBuyNotHostException("공구 삭제는 공구의 주최자만 요청 가능합니다.");
        }

        groupBuyRepository.delete(groupBuy);

    }
}
