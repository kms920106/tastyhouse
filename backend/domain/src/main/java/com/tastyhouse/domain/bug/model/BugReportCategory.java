package com.tastyhouse.domain.bug.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public enum BugReportCategory {
    PAYMENT,
    LOGIN,
    ORDER,
    RESERVATION,
    UI,
    PERFORMANCE,
    ETC;

    public static BugReportCategory from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.BUG_REPORT_CATEGORY_UNKNOWN,
                ErrorCode.BUG_REPORT_CATEGORY_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
