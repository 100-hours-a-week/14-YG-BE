package com.moogsan.moongsan_backend.domain.groupbuy.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/// 커스텀 어노테이션 정의
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PickupDateReasonValidator.class)
@Documented
public @interface RequireReasonIfPickupDateChanged {
    String message() default "픽업 일자가 변경된 경우 사유를 작성해야 합니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

