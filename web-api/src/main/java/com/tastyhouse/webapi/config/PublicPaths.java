package com.tastyhouse.webapi.config;

import org.springframework.util.AntPathMatcher;

import java.util.List;

public final class PublicPaths {

    private PublicPaths() {}

    public static final String[] PATTERNS = {
        "/api/auth/signup", "/api/auth/login", "/api/auth/refresh",
        "/api/email-verifications/**",
        "/api/phone-verifications/**",
        "/api/policies/**",
        "/api/faqs/**",
        "/api/notices/**",
        "/api/members/v1/nickname/availability",
        "/api/banners/**",
        "/api/places/**",
        "/api/event/**",
        "/api/ranks/**",
        "/api/products/**",
        "/swagger-ui/**", "/swagger-resources/**",
        "/v3/api-docs/**"
    };

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final List<String> PATTERN_LIST = List.of(PATTERNS);

    public static boolean isPublic(String requestUri) {
        return PATTERN_LIST.stream().anyMatch(pattern -> PATH_MATCHER.match(pattern, requestUri));
    }
}
