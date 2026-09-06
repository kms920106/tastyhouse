package com.tastyhouse.domain.bug.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public enum BugReportStatus {

    RECEIVED,     // 접수 (제보 등록 직후 초기 상태)
    IN_PROGRESS,  // 처리중 (담당자 확인·처리 진행)
    RESOLVED,     // 처리완료 (수정/조치 완료)
    REJECTED,     // 반려 (재현 불가·정상 동작 등)
    ON_HOLD;      // 보류 (외부 의존·후속 릴리스 대기 등)

    public static BugReportStatus from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.BUG_REPORT_STATUS_UNKNOWN,
                ErrorCode.BUG_REPORT_STATUS_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
