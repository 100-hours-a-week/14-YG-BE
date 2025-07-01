package com.moogsan.moongsan_backend.domain.groupbuy.dto.command.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.moogsan.moongsan_backend.domain.groupbuy.validator.NotBlankIfPresent;
import com.moogsan.moongsan_backend.domain.groupbuy.validator.RequireReasonIfPickupDateChanged;
import com.moogsan.moongsan_backend.global.profanity.ProfanitySafe;
import com.moogsan.moongsan_backend.global.xss.XssSafe;
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

    @ProfanitySafe
    @XssSafe
    @NotBlankIfPresent(message = TITLE_SIZE)
    @Size(min = 1, max = 100, message = TITLE_SIZE)
    private String title;

    @ProfanitySafe
    @XssSafe
    @NotBlankIfPresent(message = NAME_SIZE)
    @Size(min = 1, max = 100, message = NAME_SIZE)
    private String name;

    @ProfanitySafe
    @XssSafe
    @NotBlankIfPresent(message = DESCRIPTION_SIZE)
    @Size(min = 2, max = 2000, message = DESCRIPTION_SIZE)
    private String description;

    @Min(value = 1, message = BLANK_HOST_QUANTITY)
    private Integer hostQuantity;

    @Future(message = INVALID_DUEDATE)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime dueDate;

    @Future(message = INVALID_PICKUPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime pickupDate;

    @ProfanitySafe
    @XssSafe
    @NotBlankIfPresent(message = BLANK_DATEMODIFICATION_REASON)
    @Size(min = 2, max = 85, message = BLANK_DATEMODIFICATION_REASON)
    private String dateModificationReason;

    @Size(min=1, max = 5, message = INVALID_UPDATE_IMAGE)
    private List<
            @NotBlankIfPresent(message = INVALID_UPDATE_IMAGE)
            @Pattern(
                    regexp = "^(?:tmp|group-buys)/\\S+$",
                    message = INVALID_UPDATE_IMAGE
            ) String> imageKeys;
}
