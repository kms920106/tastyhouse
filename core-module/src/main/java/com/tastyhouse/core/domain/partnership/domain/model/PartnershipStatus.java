package com.tastyhouse.core.domain.partnership.domain.model;

import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;

public enum PartnershipStatus {
    PENDING,        // 접수 대기
    IN_PROGRESS,    // 처리 중
    COMPLETED;      // 처리 완료

    public static PartnershipStatus from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.PARTNERSHIP_STATUS_UNKNOWN,
                ErrorCode.PARTNERSHIP_STATUS_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
