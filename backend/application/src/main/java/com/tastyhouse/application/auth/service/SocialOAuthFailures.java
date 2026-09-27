package com.tastyhouse.application.auth.service;

import com.tastyhouse.application.auth.port.out.SocialOAuthFailure;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public final class SocialOAuthFailures {

    private SocialOAuthFailures() {
    }

    public static BusinessException toException(SocialOAuthFailure failure) {
        ErrorCode errorCode = switch (failure) {
            case APPLE_ID_TOKEN_INVALID -> ErrorCode.APPLE_ID_TOKEN_INVALID;
            case ACCESS_TOKEN_REJECTED -> ErrorCode.SOCIAL_OAUTH_FAILED;
        };
        return new BusinessException(errorCode);
    }
}
