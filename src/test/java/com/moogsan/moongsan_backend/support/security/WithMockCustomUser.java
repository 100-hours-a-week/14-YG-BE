package com.moogsan.moongsan_backend.support.security;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({METHOD, TYPE})
@Retention(RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserFactory.class)
public @interface WithMockCustomUser {
    long id() default 1L;
    String username() default "tester";
}
