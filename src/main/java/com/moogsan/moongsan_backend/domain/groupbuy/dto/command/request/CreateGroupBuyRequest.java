package com.moogsan.moongsan_backend.domain.groupbuy.dto.command.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.moogsan.moongsan_backend.global.profanity.ProfanitySafe;
import com.moogsan.moongsan_backend.global.xss.XssSafe;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;
import java.util.List;

import static com.moogsan.moongsan_backend.domain.groupbuy.message.ValidationMessage.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateGroupBuyRequest {

    @ProfanitySafe
    @XssSafe
    @NotNull(message = TITLE_SIZE)
    @NotBlank(message = TITLE_SIZE)
    @Size(min = 1, max = 100, message = TITLE_SIZE)
    private String title;

    @ProfanitySafe
    @XssSafe
    @NotNull(message = NAME_SIZE)
    @NotBlank(message = NAME_SIZE)
    @Size(min = 1, max = 100, message = NAME_SIZE)
    private String name;

    @ProfanitySafe
    @XssSafe
    @Size(min = 1, max = 2000, message = URL_SIZE)
    @URL(message = INVALID_URL)
    private String url;

    @NotNull(message = BLANK_PRICE)
    @Min(value = 1, message = PRICE_SIZE)
    private Integer price;

    @NotNull(message = BLANK_TOTAL_AMOUNT)
    @Min(value = 1, message = TOTAL_AMOUNT_SIZE)
    private Integer totalAmount;

    @NotNull(message = BLANK_UNIT_AMOUNT)
    @Min(value = 1, message = UNIT_AMOUNT_SIZE)
    private Integer unitAmount;

    @NotNull(message = BLANK_HOST_QUANTITY)
    @Min(value = 0, message = HOST_QUANTITY_SIZE)  /// 이후 1로 수정 필요
    private Integer hostQuantity;

    @ProfanitySafe
    @XssSafe
    @NotNull(message = DESCRIPTION_SIZE)
    @NotBlank(message = DESCRIPTION_SIZE)
    @Size(min = 2, max = 2000, message = DESCRIPTION_SIZE)
    private String description;

    @NotNull(message = INVALID_DUEDATE)
    @Future(message = INVALID_DUEDATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime dueDate;

    @NotNull(message = LOCATION_SIZE)
    @NotBlank(message = LOCATION_SIZE)
    @Size(min = 2, max = 85, message = LOCATION_SIZE)
    private String location;

    @NotNull(message = INVALID_PICKUPDATE)
    @Future(message = INVALID_PICKUPDATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime pickupDate;

    @NotNull(message = INVALID_IMAGE)
    @Size(min=1, max = 5, message = INVALID_IMAGE)
    private List<
            @NotBlank(message = INVALID_IMAGE)
            @Pattern(
                    regexp = "^.*images/.*$",
                    message = INVALID_IMAGE
            ) String> imageKeys;
}
