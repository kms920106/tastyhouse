package com.tastyhouse.domain.bug.model;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;

public enum BugReportPriority {

    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;

    public static BugReportPriority from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new DomainException(DomainErrorCode.BUG_REPORT_PRIORITY_UNKNOWN,
                DomainErrorCode.BUG_REPORT_PRIORITY_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
