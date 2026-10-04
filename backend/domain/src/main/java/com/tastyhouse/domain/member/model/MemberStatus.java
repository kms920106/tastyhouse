package com.tastyhouse.domain.member.model;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;

public enum MemberStatus {

    ACTIVE,
    SUSPENDED,
    DELETED;

    public static MemberStatus from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new DomainException(DomainErrorCode.MEMBER_STATUS_TYPE_UNKNOWN,
                DomainErrorCode.MEMBER_STATUS_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
