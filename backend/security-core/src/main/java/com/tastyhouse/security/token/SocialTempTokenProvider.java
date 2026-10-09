package com.tastyhouse.security.token;

public enum SocialTempTokenProvider {

    KAKAO("kakao_temp:"),
    NAVER("naver_temp:"),
    FACEBOOK("facebook_temp:"),
    APPLE("apple_temp:");

    private final String keyPrefix;

    SocialTempTokenProvider(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public String keyPrefix() {
        return keyPrefix;
    }
}
