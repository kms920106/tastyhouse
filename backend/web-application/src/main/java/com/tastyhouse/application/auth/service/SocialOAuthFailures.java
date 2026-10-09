package com.tastyhouse.application.auth.service;

import com.tastyhouse.application.auth.port.out.SocialOAuthFailure;
import com.tastyhouse.application.auth.port.out.SocialProvider;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.WebErrorCode;

public final class SocialOAuthFailures {

    private SocialOAuthFailures() {
    }

    public static ApplicationException toException(SocialOAuthFailure failure) {
        WebErrorCode errorCode = switch (failure) {
            case APPLE_ID_TOKEN_INVALID -> WebErrorCode.APPLE_ID_TOKEN_INVALID;
            case ACCESS_TOKEN_REJECTED -> WebErrorCode.SOCIAL_OAUTH_FAILED;
        };
        return new ApplicationException(errorCode);
    }

    public static ApplicationException tempTokenExpired(SocialProvider provider) {
        WebErrorCode errorCode = switch (provider) {
            case KAKAO -> WebErrorCode.KAKAO_TEMP_TOKEN_EXPIRED;
            case NAVER -> WebErrorCode.NAVER_TEMP_TOKEN_EXPIRED;
            case FACEBOOK -> WebErrorCode.FACEBOOK_TEMP_TOKEN_EXPIRED;
            case APPLE -> WebErrorCode.APPLE_TEMP_TOKEN_EXPIRED;
        };
        return new ApplicationException(errorCode);
    }
}
