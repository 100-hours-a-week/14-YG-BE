package com.moogsan.moongsan_backend.domain.order.controller;

import com.moogsan.moongsan_backend.domain.WrapperResponse;
import com.moogsan.moongsan_backend.domain.order.dto.request.OrderStatusUpdateRequest;
import com.moogsan.moongsan_backend.domain.order.dto.response.OrderCreateResponse;
import com.moogsan.moongsan_backend.domain.order.dto.response.OrderParticipantResponse;
import com.moogsan.moongsan_backend.domain.order.service.OrderStatusUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.moogsan.moongsan_backend.domain.order.dto.request.OrderCreateRequest;
import com.moogsan.moongsan_backend.domain.order.service.OrderCreateService;
import com.moogsan.moongsan_backend.domain.order.service.OrderQueryService;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import com.moogsan.moongsan_backend.domain.user.entity.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderController {

    private final OrderCreateService orderCreateService;
    private final OrderStatusUpdateService orderStatusUpdateService;
    private final OrderQueryService orderQueryService;

    // 주문 등록
    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(
            @Valid @RequestBody OrderCreateRequest orderCreateRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        OrderCreateResponse responseData = orderCreateService.createOrder(orderCreateRequest, userDetails.getUser().getId());
        return ResponseEntity.status(201).body(
            WrapperResponse.<OrderCreateResponse>builder()
                .message("주문이 성공적으로 등록되었습니다.")
                .data(responseData)
                .build()
        );
    }

    // 본인 주문 조회
    @GetMapping("/orders/{postId}")
    public ResponseEntity<?> getOrder(
            @PathVariable("postId") Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        OrderCreateResponse response = orderQueryService.getOrderIfNotCanceledOrRefunded(postId, userId);

        if (response == null) {
            return ResponseEntity.status(404).body(
                WrapperResponse.<OrderCreateResponse>builder()
                        .message("해당 주문이 존재하지 않거나 취소되었습니다.")
                        .data(null)
                        .build()
            );
        }

        return ResponseEntity.ok(
                WrapperResponse.<OrderCreateResponse>builder()
                        .message("주문 정보 조회에 성공하였습니다.")
                        .data(response)
                        .build());
    }

    // 주문 상태 일괄 변경
    @PatchMapping("/orders/statuses")
    public ResponseEntity<?> bulkUpdateOrderStatus(
            @RequestBody @Valid List<OrderStatusUpdateRequest> requests
    ) {
        orderStatusUpdateService.updateOrderStatuses(requests);

        return ResponseEntity.ok(
                WrapperResponse.<Void>builder()
                        .message("주문 상태가 성공적으로 변경되었습니다.")
                        .data(null)
                        .build()
        );
    }

    // 주문 참여자 전체 조회
    @GetMapping("/orders/{postId}/participants")
    public ResponseEntity<?> getOrderParticipants(
            @PathVariable Long postId
    ) {
        List<OrderParticipantResponse> responses = orderQueryService.getParticipantsByPostId(postId);
        return ResponseEntity.ok(
                WrapperResponse.<List<OrderParticipantResponse>>builder()
                        .message("주문 참여자 목록 조회에 성공하였습니다.")
                        .data(responses)
                        .build()
        );
    }
}
