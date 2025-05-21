package com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyCommandService;

import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.groupbuy.policy.DueSoonPolicy;
import com.moogsan.moongsan_backend.domain.groupbuy.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.domain.order.entity.Order;
import com.moogsan.moongsan_backend.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CancelGroupBuyParticipant {

    private final OrderRepository orderRepository;
    private final GroupBuyRepository groupBuyRepository;
    private final DueSoonPolicy dueSoonPolicy;

    /**
     * 참여 후 1일이 지났지만 상태가 CONFIRMED가 아닌 주문을 자동 취소
     */
    @Transactional
    public void cancelUnconfirmedOrders() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(1);

        List<Order> staleOrders = orderRepository.findAllByCreatedAtBeforeAndStatusNotAndStatusNot(
                threshold, "CONFIRMED", "CANCELED");

        log.info("🔄 자동 취소 대상 주문 수: {}", staleOrders.size());

        for (Order order : staleOrders) {
            GroupBuy groupBuy = order.getGroupBuy();

            // 참여자 수 및 남은 수량 복구
            groupBuy.increaseLeftAmount(order.getQuantity());
            groupBuy.decreaseParticipantCount();

            // 주문 상태 변경
            order.setStatus("CANCELED");

            // dueSoon 상태 업데이트
            groupBuy.updateDueSoonStatus(dueSoonPolicy);

            log.info("❌ 자동 취소됨 - 주문 ID: {}, 유저 ID: {}", order.getId(), order.getUser().getId());
        }

        // 한 번에 saveAll로 저장해도 됨 (최적화)
        orderRepository.saveAll(staleOrders);
    }
}
