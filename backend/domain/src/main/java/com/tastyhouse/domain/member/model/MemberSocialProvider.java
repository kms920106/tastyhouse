package com.tastyhouse.domain.member.model;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;

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
            throw new DomainException(DomainErrorCode.SOCIAL_PROVIDER_TYPE_UNKNOWN,
                DomainErrorCode.SOCIAL_PROVIDER_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
