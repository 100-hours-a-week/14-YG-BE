package com.moogsan.moongsan_backend.domain.order.service;

import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.groupbuy.policy.DueSoonPolicy;
import com.moogsan.moongsan_backend.domain.groupbuy.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.domain.order.dto.request.OrderCreateRequest;
import com.moogsan.moongsan_backend.domain.order.dto.response.OrderCreateResponse;
import com.moogsan.moongsan_backend.domain.order.entity.Order;
import com.moogsan.moongsan_backend.domain.order.repository.OrderRepository;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import com.moogsan.moongsan_backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.moogsan.moongsan_backend.global.exception.base.BusinessException;
import com.moogsan.moongsan_backend.global.exception.code.ErrorCode;

@Service
@RequiredArgsConstructor
public class OrderCreateService {

    private final UserRepository userRepository;
    private final GroupBuyRepository groupBuyRepository;
    private final OrderRepository orderRepository;
    private final DueSoonPolicy dueSoonPolicy;

    // 주문 생성 서비스
    @Transactional
    public OrderCreateResponse createOrder(OrderCreateRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "유저 정보를 찾을 수 없습니다."));

        GroupBuy groupBuy = groupBuyRepository.findById(request.getPostId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "공동구매 정보를 찾을 수 없습니다."));

        // 공동구매 글의 상태가 열려있어야지만 주문 가능
        if (!"OPEN".equals(groupBuy.getPostStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "현재 주문이 불가능한 상태입니다.");
        }

        // 동일 postId, userId로 CANCELED 주문이 3건 이상이면 차단
        long canceledCount = orderRepository.countByUserIdAndGroupBuyIdAndStatus(user.getId(), groupBuy.getId(), "CANCELED");
        if (canceledCount > 3) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "주문을 3회 이상 취소하였습니다.");
        }

        // 해당 공구 내 CANCELED 상태가 아닌 주문 존재
        orderRepository.findByUserIdAndGroupBuyIdAndStatusNot(user.getId(), groupBuy.getId(), "CANCELED")
            .ifPresent(o -> {
                throw new BusinessException(ErrorCode.DUPLICATE_REQUEST, "이미 공동구매에 참여하였습니다.");
            });

        // 입력 수량이 해당 공구의 주문 단위의 배수가 아님
        if (request.getQuantity() % groupBuy.getUnitAmount() != 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "수량은 주문 단위의 배수여야 합니다.");
        }

        // 입력 수량이 해당 공구의 남은 수량을 초과
        if (request.getQuantity() > groupBuy.getLeftAmount()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "남은 수량을 초과하여 주문할 수 없습니다.");
        }

        String orderName = request.getName() != null ? request.getName() : user.getName();

        Order order = Order.builder()
                .user(user)
                .groupBuy(groupBuy)
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .name(orderName)
                .build();

        groupBuy.decreaseLeftAmount(request.getQuantity());
        groupBuy.increaseParticipantCount();
        groupBuy.updateDueSoonStatus(dueSoonPolicy);

        if (groupBuy.getLeftAmount() == 0) {
            groupBuy.changePostStatus("CLOSED");
        }

        groupBuyRepository.save(groupBuy);
        orderRepository.save(order);

        return OrderCreateResponse.builder()
            .productName(groupBuy.getName())
            .quantity(order.getQuantity())
            .price(order.getPrice())
            .hostName(groupBuy.getUser().getName())
            .hostAccountBank(groupBuy.getUser().getAccountBank())
            .hostAccountNumber(groupBuy.getUser().getAccountNumber())
            .build();
    }

    // 주문 조회 서비스
    public OrderCreateResponse getOrderIfNotCanceled(Long postId, Long userId) {
        GroupBuy groupBuy = groupBuyRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "공동구매 정보를 찾을 수 없습니다."));

        Order order = orderRepository.findByUserIdAndGroupBuyIdAndStatusNot(userId, postId, "CANCELED")
                .orElse(null);

        if (order == null) return null;

        return OrderCreateResponse.builder()
                .productName(groupBuy.getName())
                .quantity(order.getQuantity())
                .price(order.getPrice())
                .hostName(groupBuy.getUser().getName())
                .hostAccountBank(groupBuy.getUser().getAccountBank())
                .hostAccountNumber(groupBuy.getUser().getAccountNumber())
                .build();
    }
}
