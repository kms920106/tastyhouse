package com.tastyhouse.core.domain.event.domain.model;

import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;

public enum EventStatus {
    SCHEDULED,  // 예정
    ACTIVE,     // 진행중
    ENDED;      // 종료

    public static EventStatus from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.EVENT_STATUS_UNKNOWN,
                ErrorCode.EVENT_STATUS_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
