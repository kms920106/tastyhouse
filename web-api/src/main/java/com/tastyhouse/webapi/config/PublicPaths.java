package com.tastyhouse.webapi.config;

import org.springframework.util.AntPathMatcher;

import java.util.List;

public final class PublicPaths {

    private PublicPaths() {}

    public static final String[] PATTERNS = {
        "/api/auth/v1/signup", "/api/auth/v1/login", "/api/auth/v1/refresh",
        "/api/auth/v1/password-reset/request", "/api/auth/v1/password-reset/verify", "/api/auth/v1/password-reset/confirm",
        "/api/auth/v1/login/kakao", "/api/auth/v1/signup/kakao", "/api/auth/v1/login/phone", "/api/auth/v1/link/kakao",
        "/api/email-verifications/**",
        "/api/phone-verifications/**",
        "/api/policies/**",
        "/api/faqs/**",
        "/api/notices/**",
        "/api/members/v1/phone/availability", "/api/members/v1/nickname/availability",
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
