package com.moogsan.moongsan_backend.global.xss;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

@Aspect
@Component
public class XssSanitizerAspect {
    @Around("@annotation(org.springframework.web.bind.annotation.PostMapping) ||"
            + "@annotation(org.springframework.web.bind.annotation.PutMapping) ||"
            + "@annotation(org.springframework.web.bind.annotation.PatchMapping)")
    public Object sanitizeAnnotatedFields(ProceedingJoinPoint joinPoint) throws Throwable {
        for (Object arg : joinPoint.getArgs()) {

            if (arg == null) continue;

            for (Field field : arg.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(XssSafe.class) && field.getType().equals(String.class)) {
                    field.setAccessible(true);
                    String raw = (String) field.get(arg);
                    if (raw != null) {
                        field.set(arg, XssSanitizer.sanitize(raw));
                    }
                }
            }
        }
        return joinPoint.proceed();
    }

}
