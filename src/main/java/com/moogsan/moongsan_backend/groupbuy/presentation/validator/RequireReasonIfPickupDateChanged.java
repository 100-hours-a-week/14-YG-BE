package com.moogsan.moongsan_backend.groupbuy.presentation.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

import static com.moogsan.moongsan_backend.groupbuy.domain.message.ValidationMessage.BLANK_DATEMODIFICATION_REASON;

/// 커스텀 어노테이션 정의
@Target({ ElementType.TYPE, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PickupDateReasonValidator.class)
@Documented
public @interface RequireReasonIfPickupDateChanged {
    String message() default BLANK_DATEMODIFICATION_REASON;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

