package com.moogsan.moongsan_backend.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MyPageImageRequest {

    @NotBlank
    private String imageUrl;
}
