package com.tastyhouse.webapi.config.security;

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
        "/api/shops/v1/map/markers", "/api/shops/v1/best", "/api/shops/v1/editor-choice", "/api/shops/v1/latest", "/api/shops/v1/stations", "/api/shops/v1/food-types", "/api/shops/v1/amenities", "/api/shops/v1/*", "/api/shops/v1/*/info", "/api/shops/v1/*/banners", "/api/shops/v1/*/products", "/api/shops/v1/*/photos", "/api/shops/v1/*/reviews", "/api/shops/v1/*/reviews/statistics", "/api/shops/v1/*/bookmark", "/api/shops/v1/*/order-methods", "/api/shops/v1/*/delivery-tip", "/api/shops/v1/*/scheduled-order-slots",
        "/api/policies/**",
        "/api/products/**",
        "/api/follows/v1/*/following/public", "/api/follows/v1/*/followers/public",
        "/api/ranks/v1/duration", "/api/ranks/v1/prizes", "/api/ranks/v1/members",
        "/api/reviews/**",
        "/api/search/v1/popular-keywords", "/api/search/v1/recommended-keywords", "/api/search/v1/menus", "/api/search/v1/reviews", "/api/search/v1/shops/public",
        "/api/mail-verifications/**",
        "/api/sms-verifications/**",
        "/swagger-ui/**", "/swagger-resources/**",
        "/v3/api-docs/**"
    };
}
