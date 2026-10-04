package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;

public enum SuspensionReason {

    EARLY_CLOSE("조기종료"),
    OPEN_DELAY("오픈지연"),
    SHOP_CIRCUMSTANCE("가게사정"),
    UNREACHABLE("연락불가"),
    TERMINATION_REQUEST("해지요청"),
    BAD_WEATHER("기상악화");

    private final String description;

    SuspensionReason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public static SuspensionReason from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new DomainException(DomainErrorCode.SHOP_SUSPENSION_REASON_UNKNOWN,
                DomainErrorCode.SHOP_SUSPENSION_REASON_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
