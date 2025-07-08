package com.moogsan.moongsan_backend.domain.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

@Getter
@Builder
@AllArgsConstructor
public class OrderCreateResponse {
    private Long orderId;
    private String productName;
    private Integer quantity;
    private Integer price;
    private String hostName;
    private String hostAccountBank;
    private String hostAccountNumber;
}
