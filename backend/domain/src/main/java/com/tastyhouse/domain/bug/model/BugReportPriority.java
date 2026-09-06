package com.tastyhouse.domain.bug.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public enum BugReportPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;

    public static BugReportPriority from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.BUG_REPORT_PRIORITY_UNKNOWN,
                ErrorCode.BUG_REPORT_PRIORITY_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
