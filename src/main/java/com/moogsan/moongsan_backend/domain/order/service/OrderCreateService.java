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
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
@RequiredArgsConstructor
public class OrderCreateService {

    private final UserRepository userRepository;
    private final GroupBuyRepository groupBuyRepository;
    private final OrderRepository orderRepository;
    private final DueSoonPolicy dueSoonPolicy;

    @Transactional
    public OrderCreateResponse createOrder(OrderCreateRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "유저 정보를 찾을 수 없습니다."));

        GroupBuy groupBuy = groupBuyRepository.findById(request.getPostId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "공동구매 정보를 찾을 수 없습니다."));

        // 공동구매 글의 상태가 열려있어야지만 주문 가능
        if (!"OPEN".equals(groupBuy.getPostStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "현재 주문이 불가능한 상태입니다.");
        }

        // 해당 공구 내 CANCELED 상태가 아닌 주문 존재
        orderRepository.findByUserIdAndGroupBuyIdAndStatusNot(user.getId(), groupBuy.getId(), "CANCELED")
            .ifPresent(o -> {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 공동구매에 참여하였습니다.");
            });

        // 입력 수량이 해당 공구의 주문 단위의 배수가 아님
        if (request.getQuantity() % groupBuy.getUnitAmount() != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수량은 주문 단위의 배수여야 합니다.");
        }

        // 입력 수량이 해당 공구의 남은 수량을 초과
        if (request.getQuantity() > groupBuy.getLeftAmount()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "남은 수량을 초과하여 주문할 수 없습니다.");
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
}
