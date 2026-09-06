package com.tastyhouse.domain.bug.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

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
            throw new BusinessException(ErrorCode.BUG_REPORT_STATUS_UNKNOWN,
                ErrorCode.BUG_REPORT_STATUS_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
