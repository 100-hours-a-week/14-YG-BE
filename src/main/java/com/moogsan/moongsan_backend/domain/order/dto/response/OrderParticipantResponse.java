package com.moogsan.moongsan_backend.domain.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class OrderParticipantResponse {
    private Long orderId;
    private String nickname;
    private String name;
    private String accountName;
    private String accountNumber;
    private Integer price;
    private Integer quantity;
    private String status;
}
