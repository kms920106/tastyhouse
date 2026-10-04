package com.tastyhouse.infrastructure.facebook.oauth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth.facebook")
record FacebookOAuthProperties(
    String appId,
    String appSecret
) {

    public FacebookOAuthProperties {
        requireResolved("oauth.facebook.app-id", appId);
        requireResolved("oauth.facebook.app-secret", appSecret);
    }

    private static void requireResolved(String key, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(key + " 설정값이 비어 있습니다. 환경변수가 .env 또는 실행 환경에 설정돼 있는지 확인하세요.");
        }
        if (value.contains("${")) {
            throw new IllegalStateException(key + " 설정값의 환경변수가 해석되지 않았습니다: " + value);
        }
    }
}
