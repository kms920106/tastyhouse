package com.tastyhouse.domain.event.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public enum EventStatus {
    SCHEDULED,
    ACTIVE,
    ENDED;

    public static EventStatus from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.EVENT_STATUS_UNKNOWN,
                ErrorCode.EVENT_STATUS_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
