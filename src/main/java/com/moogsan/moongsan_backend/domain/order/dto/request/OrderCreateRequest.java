package com.moogsan.moongsan_backend.domain.order.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderCreateRequest {

    @NotNull
    private Long postId;

    @NotNull
    private Integer price;

    @NotNull
    private Integer quantity;

    private String name;
}
