package com.moogsan.moongsan_backend.domain.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderStatusUpdateRequest {
    @NotBlank(message = "상태값은 필수입니다.")
    private String status;
}
