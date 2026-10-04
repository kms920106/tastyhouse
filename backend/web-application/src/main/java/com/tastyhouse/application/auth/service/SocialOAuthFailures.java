package com.tastyhouse.application.auth.service;

import com.tastyhouse.application.auth.port.out.SocialOAuthFailure;
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
}
