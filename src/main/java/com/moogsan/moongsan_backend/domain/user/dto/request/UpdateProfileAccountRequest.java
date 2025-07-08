package com.moogsan.moongsan_backend.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateProfileAccountRequest {

    @NotBlank(message = "은행명은 필수입니다.")
    private String accountBank; // 계좌 은행명

    @NotBlank(message = "계좌번호는 필수입니다.")
    @Pattern(regexp = "^\\d+$", message = "계좌번호는 숫자만 입력해야 합니다.")
    private String accountNumber; // 계좌 번호

    @NotBlank(message = "실명은 필수입니다.")
    private String name; // 예금주 이름
}
