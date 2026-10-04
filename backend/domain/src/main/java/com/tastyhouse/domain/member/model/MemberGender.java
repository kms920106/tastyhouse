package com.tastyhouse.domain.member.model;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;

public enum MemberGender {

    MALE,
    FEMALE;

    public static MemberGender from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new DomainException(DomainErrorCode.GENDER_TYPE_UNKNOWN,
                DomainErrorCode.GENDER_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
