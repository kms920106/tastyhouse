package com.tastyhouse.core.domain.member.domain.model;

import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;

public enum SocialProvider {

    KAKAO,
    NAVER,
    FACEBOOK,
    GOOGLE,
    APPLE;

    public static SocialProvider from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.SOCIAL_PROVIDER_TYPE_UNKNOWN,
                ErrorCode.SOCIAL_PROVIDER_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
