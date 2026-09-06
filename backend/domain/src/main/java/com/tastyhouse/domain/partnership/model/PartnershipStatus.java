package com.tastyhouse.domain.partnership.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public enum PartnershipStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED;

    public static PartnershipStatus from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.PARTNERSHIP_STATUS_UNKNOWN,
                ErrorCode.PARTNERSHIP_STATUS_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
