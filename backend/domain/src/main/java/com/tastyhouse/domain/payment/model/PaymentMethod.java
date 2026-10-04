package com.tastyhouse.domain.payment.model;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;

public enum PaymentMethod {

    CASH_ON_SITE,
    CARD_ON_SITE,
    CREDIT_CARD,
    MOBILE,
    KAKAO_PAY,
    ZERO_PAY;

    public static PaymentMethod from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new DomainException(DomainErrorCode.PAYMENT_METHOD_UNKNOWN,
                DomainErrorCode.PAYMENT_METHOD_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
