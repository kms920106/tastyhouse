package com.tastyhouse.domain.member.domain.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public enum MemberSocialProvider {

    KAKAO,
    NAVER,
    FACEBOOK,
    GOOGLE,
    APPLE;

    public static MemberSocialProvider from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.SOCIAL_PROVIDER_TYPE_UNKNOWN,
                ErrorCode.SOCIAL_PROVIDER_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
