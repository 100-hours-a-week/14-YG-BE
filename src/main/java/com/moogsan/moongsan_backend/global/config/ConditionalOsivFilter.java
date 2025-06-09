package com.moogsan.moongsan_backend.global.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter;
import org.springframework.stereotype.Component;

@Component
public class ConditionalOsivFilter extends OpenEntityManagerInViewFilter {
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().endsWith("/latest");
    }
}
