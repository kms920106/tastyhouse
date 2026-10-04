package com.tastyhouse.domain.bug.model;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;

public enum BugReportStatus {

    RECEIVED,
    IN_PROGRESS,
    RESOLVED,
    REJECTED,
    ON_HOLD;

    public static BugReportStatus from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new DomainException(DomainErrorCode.BUG_REPORT_STATUS_UNKNOWN,
                DomainErrorCode.BUG_REPORT_STATUS_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
