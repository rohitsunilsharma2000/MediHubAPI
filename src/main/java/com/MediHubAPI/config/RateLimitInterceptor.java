package com.MediHubAPI.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    // You can customize real limits using Redis or in-memory counters.
    private static final String LIMIT = "100";
    private static final String REMAINING = "98";
    private static final String RESET_SECONDS = "60";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        response.setHeader("X-RateLimit-Limit", LIMIT);
        response.setHeader("X-RateLimit-Remaining", REMAINING);
        response.setHeader("X-RateLimit-Reset", RESET_SECONDS);
        response.setHeader("X-Request-ID", UUID.randomUUID().toString());
        return true;
    }
}
