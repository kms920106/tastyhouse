package com.tastyhouse.core.domain.bug.domain.model;

import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;

public enum BugReportPriority {

    LOW,       // 낮음
    MEDIUM,    // 보통
    HIGH,      // 높음
    CRITICAL;  // 심각 (서비스 중대 장애)

    public static BugReportPriority from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.BUG_REPORT_PRIORITY_UNKNOWN,
                ErrorCode.BUG_REPORT_PRIORITY_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
