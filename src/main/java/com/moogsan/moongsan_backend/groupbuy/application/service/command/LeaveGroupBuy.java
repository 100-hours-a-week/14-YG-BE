package com.moogsan.moongsan_backend.groupbuy.application.service.command;

import com.moogsan.moongsan_backend.domain.order.service.OrderEventService;
import com.moogsan.moongsan_backend.groupbuy.domain.service.GroupBuyEventService;
import com.moogsan.moongsan_backend.participantchat.application.facade.command.ChattingCommandFacade;
import com.moogsan.moongsan_backend.groupbuy.domain.entity.GroupBuy;
import com.moogsan.moongsan_backend.groupbuy.domain.exception.specific.GroupBuyInvalidStateException;
import com.moogsan.moongsan_backend.groupbuy.domain.exception.specific.GroupBuyNotFoundException;
import com.moogsan.moongsan_backend.groupbuy.domain.service.DueSoonPolicy;
import com.moogsan.moongsan_backend.groupbuy.domain.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.domain.order.entity.Order;
import com.moogsan.moongsan_backend.domain.order.exception.specific.OrderNotFoundException;
import com.moogsan.moongsan_backend.domain.order.repository.OrderRepository;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.moogsan.moongsan_backend.groupbuy.domain.message.ResponseMessage.NOT_OPEN;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class LeaveGroupBuy {

    private final GroupBuyRepository groupBuyRepository;
    private final OrderRepository orderRepository;
    private final DueSoonPolicy dueSoonPolicy;
    private final ChattingCommandFacade chattingCommandFacade;
    private final GroupBuyEventService groupBuyEventService;
    private final OrderEventService orderEventService;
    private final Clock clock;
    private final RedisTemplate<String, String> redisTemplate;
    private final RedissonClient redissonClient;

    /// 공구 참여 취소
    public void leaveGroupBuy(User currentUser, Long postId) {

        // 유효성 검사
        GroupBuy groupBuy = fetchAndValidate(currentUser.getId(), postId);

        // 참여자 채팅방 나가기
        chattingCommandFacade.leaveChatRoom(currentUser, postId);

        // 주문 상태 변경
        Order order = updateOrder(currentUser.getId(), groupBuy);

        // 공구 상태 변경 이벤트 발행
        groupBuyEventService.publishGroupBuyUpdated(groupBuy);

        // 주문 취소 이벤트 발행
        orderEventService.publishOrderStatusCanceled(order, groupBuy);
    }

    private GroupBuy fetchAndValidate(Long userId, Long postId) {
        // 해당 공구가 존재하는지 조회 -> 없으면 404
        GroupBuy groupBuy = groupBuyRepository.findById(postId)
                .orElseThrow(GroupBuyNotFoundException::new);

        // 해당 공구가 OPEN인지 조회, dueDate가 현재 이후인지 조회 -> 아니면 409
        if (!groupBuy.getPostStatus().equals("OPEN")
                || groupBuy.getDueDate().isBefore(LocalDateTime.now(clock))) {
            throw new GroupBuyInvalidStateException(NOT_OPEN);
        }

        return groupBuy;
    }

    private Order updateOrder(Long userId, GroupBuy groupBuy) {
        // 해당 공구의 주문 테이블에 해당 유저의 주문이 존재하는지 조회 -> 아니면 404
        Order order = orderRepository.findByUserIdAndGroupBuyIdAndStatusNotIn(userId, groupBuy.getId(),
                        List.of("CANCELED", "REFUNDED"))
                .orElseThrow(OrderNotFoundException::new);

        // 남은 수량, 참여 인원 수 업데이트
        int returnQuantity = order.getQuantity();
        groupBuy.increaseLeftAmount(returnQuantity);
        groupBuy.decreaseParticipantCount();

        // 해당 유저의 주문을 취소
        order.setStatus("CANCELED");

        groupBuy.updateDueSoonStatus(dueSoonPolicy);

//        String stockKey = "order:groupbuy:stock:" + groupBuy.getId();
//        String orderCheckKey = "order:user:" + userId + ":groupbuy:" + groupBuy.getId();
//        String lockKey = "order:lock:groupbuy:" + groupBuy.getId();
//        // 재고 및 중복 주문 방지를 위한 분산 락 획득
//        RLock lock = redissonClient.getLock(lockKey);
//
//        try {
//            if (!lock.tryLock(3, 2, TimeUnit.SECONDS)) {
//                throw new RuntimeException("잠시 후 다시 시도해주세요."); // Consider using a proper BusinessException
//            }
//            // 주문 수량만큼 Redis 재고를 복구
//            redisTemplate.opsForValue().increment(stockKey, returnQuantity);
//            // 해당 유저의 중복 주문 체크 키 제거
//            redisTemplate.delete(orderCheckKey);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            throw new RuntimeException("락 획득 중 오류 발생"); // Consider using a proper BusinessException
//        } finally {
//            if (lock.isHeldByCurrentThread()) lock.unlock();
//        }

        orderRepository.save(order);

        return order;
    }
}
