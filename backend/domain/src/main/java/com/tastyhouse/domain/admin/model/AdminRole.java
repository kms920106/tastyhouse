package com.tastyhouse.domain.admin.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 관리자 권한 등급
 * - SUPER_ADMIN: 최고관리자. 관리자 계정 생성 등 모든 권한 보유
 * - ADMIN: 일반 관리자
 */
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
