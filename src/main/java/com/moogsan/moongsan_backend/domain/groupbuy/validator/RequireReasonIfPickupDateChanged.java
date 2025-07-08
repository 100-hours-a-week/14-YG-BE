package com.moogsan.moongsan_backend.domain.groupbuy.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

import static com.moogsan.moongsan_backend.domain.groupbuy.message.ValidationMessage.BLANK_DATEMODIFICATION_REASON;

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

