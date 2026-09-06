package com.tastyhouse.domain.admin.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public enum AdminRole {
    SUPER_ADMIN,
    ADMIN;

    public static AdminRole from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.ADMIN_ROLE_UNKNOWN,
                ErrorCode.ADMIN_ROLE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
