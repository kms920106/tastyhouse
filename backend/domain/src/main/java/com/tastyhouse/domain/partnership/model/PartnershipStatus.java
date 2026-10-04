package com.tastyhouse.domain.partnership.model;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;

public enum PartnershipStatus {

    PENDING,
    IN_PROGRESS,
    COMPLETED;

    public static PartnershipStatus from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new DomainException(DomainErrorCode.PARTNERSHIP_STATUS_UNKNOWN,
                DomainErrorCode.PARTNERSHIP_STATUS_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
