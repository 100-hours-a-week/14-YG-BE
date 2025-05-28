package com.moogsan.moongsan_backend.domain.user.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUpRequest {

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일의 형식이 올바르지 않습니다.")
    private String email; // 이메일

    @Size(min = 8, max = 30, message = "비밀번호는 8자 이상 30자 이하여야 합니다.")
    @Pattern(
            regexp = "^(?=.*[!@#$%^*+=-])[A-Za-z\\d!@#$%^*+=-]{8,30}$",
            message = "비밀번호는 8~30자이며, 반드시 하나 이상의 특수문자(!@#$%^*+=-)를 포함해야 합니다."
    )
    private String password; // 비밀번호

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 12, message = "닉네임은 2자 이상 12자 이하여야 합니다.")
    private String nickname; // 닉네임

    @NotBlank(message = "이름은 필수입니다.")
    @Size(min = 2, max = 50, message = "이름은 2자 이상 50자 이하여야 합니다.")
    private String name; // 실명

    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(regexp = "^\\d{10,11}$", message = "전화번호는 하이픈(-) 없이 10~11자리 숫자여야 합니다.")
    private String phoneNumber; // 전화번호

    @NotBlank(message = "은행명은 필수입니다.")
    private String accountBank; // 계좌 은행명

    @NotBlank(message = "계좌번호는 필수입니다.")
    @Pattern(regexp = "^\\d+$", message = "계좌번호는 숫자만 입력해야 합니다.")
    private String accountNumber; // 계좌 번호

    @JsonProperty("imageUrl")
    private String imageUrl; // 프로필 이미지 URL
}