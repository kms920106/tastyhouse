package com.tastyhouse.domain.member.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public enum MemberStatus {
    ACTIVE,      // 가입
    SUSPENDED,   // 정지
    DELETED;     // 탈퇴

    public static MemberStatus from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.MEMBER_STATUS_TYPE_UNKNOWN,
                ErrorCode.MEMBER_STATUS_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
