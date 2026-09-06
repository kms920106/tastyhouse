package com.tastyhouse.domain.payment.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

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
            throw new BusinessException(ErrorCode.PAYMENT_METHOD_UNKNOWN,
                ErrorCode.PAYMENT_METHOD_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
