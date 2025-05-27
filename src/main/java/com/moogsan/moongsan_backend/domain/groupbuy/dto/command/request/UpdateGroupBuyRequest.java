package com.moogsan.moongsan_backend.domain.groupbuy.dto.command.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.moogsan.moongsan_backend.domain.groupbuy.validator.RequireReasonIfPickupDateChanged;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.validator.constraints.URL;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RequireReasonIfPickupDateChanged
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateGroupBuyRequest {

    @Size(min = 1, max = 30, message = "제목은 1자 이상, 30자 이하로 입력해주세요.")
    private String title;

    @Size(min = 1, max = 30, message = "상품명은 1자 이상, 30자 이하로 입력해주세요.")
    private String name;

    @Size(min = 1, max = 2000, message = "URL은 1자 이상, 2000자 이하로 입력해주세요.")
    @URL(message = "URL 형식이 올바르지 않습니다.")
    private String url;

    @Size(min = 2, max = 2000, message = "상품 설명은 2자 이상, 2000자 이하로 입력해주세요.")
    private String description;

    @Future(message = "마감일자는 현재 시간 이후여야 합니다.")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime dueDate;

    @Future(message = "픽업 일자는 현재 시간 이후여야 합니다.")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime pickupDate;

    @Size(min = 2, max = 85, message = "픽업 일자 변경 사유는 2자 이상, 85자 이하로 입력해주세요.")
    private String dateModificationReason;

    @Size(min=1, max = 5, message = "이미지는 1장 이상, 5장 이하까지 등록할 수 있습니다.")
    private List<String> imageKeys;
}
