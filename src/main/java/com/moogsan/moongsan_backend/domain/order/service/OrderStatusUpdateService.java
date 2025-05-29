package com.moogsan.moongsan_backend.domain.order.service;

import com.moogsan.moongsan_backend.domain.order.entity.Order;
import com.moogsan.moongsan_backend.domain.order.repository.OrderRepository;
import com.moogsan.moongsan_backend.global.exception.code.ErrorCode;
import com.moogsan.moongsan_backend.global.exception.base.BusinessException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderStatusUpdateService {
    private final OrderRepository orderRepository;

    @Transactional
    public void updateOrderStatus(Long postId, Long userId, String status) {
        Order order = orderRepository.findByUserIdAndGroupBuyIdAndStatusNot(userId, postId, "CANCELED")
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "주문을 찾을 수 없습니다."));

        order.updateStatus(status);
    }
}
