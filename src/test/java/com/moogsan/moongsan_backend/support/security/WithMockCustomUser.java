package com.moogsan.moongsan_backend.support.security;

import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.test.context.TestExecutionListeners;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({METHOD, TYPE})
@Retention(RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserFactory.class)
@TestExecutionListeners(
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS,
        listeners = org.springframework.security.test.context.support.ReactorContextTestExecutionListener.class
)
public @interface WithMockCustomUser {
    long id() default 1L;
    String username() default "tester";
}
