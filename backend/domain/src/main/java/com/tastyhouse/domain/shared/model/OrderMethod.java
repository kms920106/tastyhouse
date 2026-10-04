package com.tastyhouse.domain.shared.model;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;

public enum OrderMethod {

    TABLE("테이블 오더"),
    RESERVATION("예약"),
    DELIVERY("배달"),
    TAKEOUT("포장");

    private final String displayName;

    OrderMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public static OrderMethod from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new DomainException(DomainErrorCode.ORDER_METHOD_UNKNOWN,
                DomainErrorCode.ORDER_METHOD_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
