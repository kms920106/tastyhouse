package com.tastyhouse.domain.admin.model;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;

public enum AdminRole {

    SUPER_ADMIN,
    ADMIN;

    public static AdminRole from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new DomainException(DomainErrorCode.ADMIN_ROLE_UNKNOWN,
                DomainErrorCode.ADMIN_ROLE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
