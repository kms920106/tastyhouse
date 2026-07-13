package com.tastyhouse.core.domain.bug.domain.model;

import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;

public enum BugReportCategory {

    PAYMENT,      // 결제
    LOGIN,        // 로그인/인증
    ORDER,        // 주문
    RESERVATION,  // 예약
    UI,           // 화면/UI
    PERFORMANCE,  // 성능/속도
    ETC;          // 기타

    public static BugReportCategory from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.BUG_REPORT_CATEGORY_UNKNOWN,
                ErrorCode.BUG_REPORT_CATEGORY_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
