package com.tastyhouse.domain.bug.domain.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public enum BugReportPlatform {

    IOS,      // iOS
    ANDROID;  // Android

    public static BugReportPlatform from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.BUG_REPORT_PLATFORM_UNKNOWN,
                ErrorCode.BUG_REPORT_PLATFORM_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
