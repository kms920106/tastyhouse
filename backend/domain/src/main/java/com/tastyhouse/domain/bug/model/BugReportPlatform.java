package com.tastyhouse.domain.bug.model;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;

public enum BugReportPlatform {

    IOS,
    ANDROID;

    public static BugReportPlatform from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new DomainException(DomainErrorCode.BUG_REPORT_PLATFORM_UNKNOWN,
                DomainErrorCode.BUG_REPORT_PLATFORM_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
