package com.moogsan.moongsan_backend.domain.order.service;

import com.moogsan.moongsan_backend.domain.order.dto.request.OrderStatusUpdateRequest;
import com.moogsan.moongsan_backend.domain.order.entity.Order;
import com.moogsan.moongsan_backend.domain.order.repository.OrderRepository;
import com.moogsan.moongsan_backend.global.exception.code.ErrorCode;
import com.moogsan.moongsan_backend.global.exception.base.BusinessException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderStatusUpdateService {
    private final OrderRepository orderRepository;

    @Transactional
    public void updateOrderStatuses(List<OrderStatusUpdateRequest> requests) {
        for (OrderStatusUpdateRequest request : requests) {
            Order order = orderRepository.findById(request.getOrderId()
            ).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "주문을 찾을 수 없습니다."));

            order.updateStatus(request.getStatus());
        }
    }
}
