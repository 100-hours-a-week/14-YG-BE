package com.moogsan.moongsan_backend.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MyPagePasswordRequest {

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Size(min = 8, max = 30, message = "비밀번호는 8자 이상 30자 이하여야 합니다.")
    @Pattern(
            regexp = "^(?=.*[!@#$%^*+=-])[A-Za-z\\d!@#$%^*+=-]{8,30}$",
            message = "비밀번호는 8~30자이며, 반드시 하나 이상의 특수문자(!@#$%^*+=-)를 포함해야 합니다."
    )
    private String password; // 비밀번호
}
