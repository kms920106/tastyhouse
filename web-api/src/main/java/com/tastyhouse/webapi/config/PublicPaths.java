package com.tastyhouse.webapi.config;

import org.springframework.util.AntPathMatcher;

import java.util.List;

public final class PublicPaths {

    private PublicPaths() {}

    public static final String[] PATTERNS = {
        "/api/auth/**",
        "/api/banners/**",
        "/api/event/**",
        "/api/faqs/**",
        "/api/grades/**",
        "/api/members/v1/phone/availability", "/api/members/v1/nickname/availability", "/api/members/v1/*/profile", "/api/members/v1/*/stats",
        "/api/notices/**",
        "/api/partnership-requests/**",
        "/api/shops/**",
        "/api/policies/**",
        "/api/products/**",
        "/api/follows/v1/*/following/public", "/api/follows/v1/*/followers/public",
        "/api/ranks/v1/duration", "/api/ranks/v1/prizes", "/api/ranks/v1/members",
        "/api/reviews/**",
        "/api/search/v1/popular-keywords", "/api/search/v1/recommended-keywords", "/api/search/v1/menus", "/api/search/v1/reviews", "/api/search/v1/shops/public",
        "/api/email-verifications/**",
        "/api/phone-verifications/**",
        "/swagger-ui/**", "/swagger-resources/**",
        "/v3/api-docs/**"
    };

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final List<String> PATTERN_LIST = List.of(PATTERNS);

    public static boolean isPublic(String requestUri) {
        return PATTERN_LIST.stream().anyMatch(pattern -> PATH_MATCHER.match(pattern, requestUri));
    }
}
