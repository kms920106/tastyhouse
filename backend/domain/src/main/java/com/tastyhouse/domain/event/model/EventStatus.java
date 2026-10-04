package com.tastyhouse.domain.event.model;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;

public enum EventStatus {

    SCHEDULED,
    ACTIVE,
    ENDED;

    public static EventStatus from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new DomainException(DomainErrorCode.EVENT_STATUS_UNKNOWN,
                DomainErrorCode.EVENT_STATUS_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
