package com.moogsan.moongsan_backend.domain.groupbuy.dto.command.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.moogsan.moongsan_backend.domain.groupbuy.validator.NotBlankIfPresent;
import com.moogsan.moongsan_backend.domain.groupbuy.validator.RequireReasonIfPickupDateChanged;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.validator.constraints.URL;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

import static com.moogsan.moongsan_backend.domain.groupbuy.message.ValidationMessage.*;

@RequireReasonIfPickupDateChanged
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateGroupBuyRequest {

    @NotBlankIfPresent(message = TITLE_SIZE)
    @Size(min = 1, max = 30, message = TITLE_SIZE)
    private String title;

    @NotBlankIfPresent(message = NAME_SIZE)
    @Size(min = 1, max = 30, message = NAME_SIZE)
    private String name;

    @NotBlankIfPresent(message = DESCRIPTION_SIZE)
    @Size(min = 2, max = 2000, message = DESCRIPTION_SIZE)
    private String description;

    @Min(value = 0, message = BLANK_HOST_QUANTITY)  /// 이후 1로 수정 필요
    private Integer hostQuantity;

    @Future(message = INVALID_DUEDATE)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime dueDate;

    @Future(message = INVALID_PICKUPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime pickupDate;

    @NotBlankIfPresent(message = BLANK_DATEMODIFICATION_REASON)
    @Size(min = 2, max = 85, message = BLANK_DATEMODIFICATION_REASON)
    private String dateModificationReason;

    @Size(min=1, max = 5, message = INVALID_IMAGE)
    private List<
            @NotBlankIfPresent(message = INVALID_IMAGE)
            @Pattern(
                    regexp = "^.*images/.*$",
                    message = INVALID_IMAGE
            ) String> imageKeys;
}
