package com.tastyhouse.domain.bug.model;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;

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
            throw new DomainException(DomainErrorCode.BUG_REPORT_CATEGORY_UNKNOWN,
                DomainErrorCode.BUG_REPORT_CATEGORY_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
