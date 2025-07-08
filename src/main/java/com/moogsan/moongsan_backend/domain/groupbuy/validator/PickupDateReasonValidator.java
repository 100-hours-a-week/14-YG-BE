package com.moogsan.moongsan_backend.domain.groupbuy.validator;

import com.moogsan.moongsan_backend.domain.groupbuy.dto.command.request.UpdateGroupBuyRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import static com.moogsan.moongsan_backend.domain.groupbuy.message.ValidationMessage.BLANK_DATEMODIFICATION_REASON;

///  validator 구현
public class PickupDateReasonValidator implements ConstraintValidator<RequireReasonIfPickupDateChanged, UpdateGroupBuyRequest> {

    @Override
    public boolean isValid(UpdateGroupBuyRequest request, ConstraintValidatorContext context) {
        // null 검사는 다른 @Valid 제약으로 처리되므로 여기서는 생략
        if (request.getPickupDate() == null) return true;

        // 기존 사용자들에게 필수적으로 픽업 일자 변경 사유를 전달해야 하므로
        // 클라이언트가 변경 요청을 할 때 항상 `dateModificationReason`을 보내야 한다고 가정
        // 즉, pickupDate가 null이 아니고, dateModificationReason이 비어 있으면 오류

        if (request.getDateModificationReason() == null || request.getDateModificationReason().trim().isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(BLANK_DATEMODIFICATION_REASON)
                    .addPropertyNode("dateModificationReason")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}

