package com.tastyhouse.webapi.config;

import org.springframework.util.AntPathMatcher;

import java.util.List;

public final class PublicPaths {

    private PublicPaths() {}

    public static final String[] PATTERNS = {
        "/api/auth/signup", "/api/auth/login", "/api/auth/refresh",
        "/api/policies/**", "/api/faqs/**", "/api/notices/**",
        "/api/banners/**", "/api/places/**", "/api/event/**",
        "/api/ranks/**", "/api/products/**",
        "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**"
    };

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final List<String> PATTERN_LIST = List.of(PATTERNS);

    public static boolean isPublic(String requestUri) {
        return PATTERN_LIST.stream().anyMatch(pattern -> PATH_MATCHER.match(pattern, requestUri));
    }
}
