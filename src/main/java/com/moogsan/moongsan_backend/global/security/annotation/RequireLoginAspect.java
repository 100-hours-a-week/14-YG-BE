package com.moogsan.moongsan_backend.global.security.annotation;

import com.moogsan.moongsan_backend.domain.user.entity.CustomUserDetails;
import com.moogsan.moongsan_backend.global.exception.specific.UnauthenticatedAccessException;
import com.moogsan.moongsan_backend.global.exception.specific.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import static com.moogsan.moongsan_backend.global.message.ResponseMessage.UNAUTHENTICATED;

@Aspect
@Component
@RequiredArgsConstructor
public class RequireLoginAspect {

    @Before("@within(package com.moogsan.moongsan_backend.global.security.annotation.RequireLogin) || " +
            "@annotation(package com.moogsan.moongsan_backend.global.security.annotation.RequireLogin)")
    public void checkAuthentication(JoinPoint jp) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null
                && auth.isAuthenticated()
                && auth.getPrincipal() instanceof CustomUserDetails;

        if(!isAuthenticated) {
            throw new UnauthenticatedAccessException();
        }
    }
}
