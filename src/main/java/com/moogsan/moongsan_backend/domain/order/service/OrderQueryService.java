package com.moogsan.moongsan_backend.domain.order.service;

import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.groupbuy.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.domain.order.dto.response.OrderCreateResponse;
import com.moogsan.moongsan_backend.domain.order.dto.response.OrderParticipantResponse;
import com.moogsan.moongsan_backend.domain.order.entity.Order;
import com.moogsan.moongsan_backend.domain.order.repository.OrderRepository;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import com.moogsan.moongsan_backend.global.exception.base.BusinessException;
import com.moogsan.moongsan_backend.global.exception.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderQueryService {

    private final GroupBuyRepository groupBuyRepository;
    private final OrderRepository orderRepository;

    // 본인 주문 조회 서비스
    public OrderCreateResponse getOrderIfNotCanceledOrRefunded(Long postId, Long userId) {
        GroupBuy groupBuy = groupBuyRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "공동구매 정보를 찾을 수 없습니다."));

        Order order = orderRepository.findByUserIdAndGroupBuyIdAndStatusNotIn(userId, postId, List.of("CANCELED", "REFUNDED"))
                .orElse(null);

        if (order == null) return null;

        return OrderCreateResponse.builder()
                .orderId(order.getId())
                .productName(groupBuy.getName())
                .quantity(order.getQuantity())
                .price(order.getPrice())
                .hostName(groupBuy.getUser().getName())
                .hostAccountBank(groupBuy.getUser().getAccountBank())
                .hostAccountNumber(groupBuy.getUser().getAccountNumber())
                .build();
    }

    // 주문 참여자 조회 서비스
    @Transactional(readOnly = true)
    public List<OrderParticipantResponse> getParticipantsByPostId(Long postId) {
        List<Order> orders = orderRepository.findAllByGroupBuyIdOrderByStatusCustom(postId);

        return orders.stream()
                .map(order -> {
                    User user = order.getUser();
                    return OrderParticipantResponse.builder()
                            .orderId(order.getId())
                            .nickname(user.getNickname())
                            .name(order.getName())
                            .accountName(user.getAccountBank())
                            .accountNumber(user.getAccountNumber())
                            .price(order.getPrice())
                            .quantity(order.getQuantity())
                            .status(order.getStatus())
                            .build();
                })
                .collect(Collectors.toList());
    }
}
