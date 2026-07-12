package com.tastyhouse.core.domain.payment.domain.model;

import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;

public enum PaymentMethod {
    CASH_ON_SITE,   // 현장 현금 결제
    CARD_ON_SITE,   // 현장 카드 결제
    CREDIT_CARD,    // 신용카드
    MOBILE,         // 휴대폰 결제
    KAKAO_PAY,      // 카카오페이
    ZERO_PAY;       // 제로페이

    public static PaymentMethod from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.PAYMENT_METHOD_UNKNOWN,
                ErrorCode.PAYMENT_METHOD_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
