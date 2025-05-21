package com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyCommandService;

import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.order.entity.Order;
import com.moogsan.moongsan_backend.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class FinalizeGroupBuy {

    private final OrderRepository orderRepository;

    public void finalizeGroupBuy(GroupBuy groupBuy) {

        Long groupBuyId = groupBuy.getId();

        // 공구에 속한 취소되지 않은 모든 주문 조회
        List<Order> validOrders = orderRepository.findByGroupBuyIdAndStatusNot(groupBuyId, "CANCELED");

        // 모든 주문이 CONFIRMED 상태인지 확인
        boolean allConfirmed = validOrders.stream()
                .allMatch(order -> order.getStatus().equals("CONFIRMED"));

        // 조건 만족 시 공구 체결 처리
        if (allConfirmed && !groupBuy.isFixed()) {
            groupBuy.setFixed(true);
        }

    }
}
