package com.moogsan.moongsan_backend.domain.user.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MyPageBasicRequest {

    @Size(min = 2, max = 12, message = "닉네임은 2자 이상 12자 이하여야 합니다.")
    private String nickname; // 닉네임

    @Size(min = 2, max = 50, message = "이름은 2자 이상 50자 이하여야 합니다.")
    private String name; // 실명

    @Pattern(regexp = "^\\d{10,11}$", message = "전화번호는 하이픈(-) 없이 10~11자리 숫자여야 합니다.")
    private String phoneNumber; // 전화번호
}
