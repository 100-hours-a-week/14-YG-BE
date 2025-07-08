package com.moogsan.moongsan_backend.domain.user.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateProfileRequest {

    @Size(min = 2, max = 12, message = "닉네임은 2자 이상 12자 이하여야 합니다.")
    private String nickname; // 닉네임

    @Pattern(regexp = "^\\d{10,11}$", message = "전화번호는 하이픈(-) 없이 10~11자리 숫자여야 합니다.")
    private String phoneNumber; // 전화번호
}
