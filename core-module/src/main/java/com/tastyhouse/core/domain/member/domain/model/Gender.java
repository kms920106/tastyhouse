package com.tastyhouse.core.domain.member.domain.model;

import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;

public enum Gender {

    MALE,
    FEMALE;

    public static Gender from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.GENDER_TYPE_UNKNOWN,
                ErrorCode.GENDER_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
