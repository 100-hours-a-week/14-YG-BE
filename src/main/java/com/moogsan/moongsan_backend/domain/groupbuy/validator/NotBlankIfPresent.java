package com.moogsan.moongsan_backend.domain.groupbuy.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

import static com.moogsan.moongsan_backend.domain.groupbuy.message.ValidationMessage.NOT_BLANK;

@Documented
@Constraint(validatedBy = NotBlankIfPresentValidator.class)
@Target({ElementType.TYPE_USE, ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotBlankIfPresent {
    String message() default NOT_BLANK;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
