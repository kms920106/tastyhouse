package com.tastyhouse.domain.payment.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public enum PgProvider {
    TOSS,
    KAKAO,
    NICE,
    KG_INICIS,
    NHN_KCP,
    SETTLE_BANK;

    public static PgProvider from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.PG_PROVIDER_UNKNOWN,
                ErrorCode.PG_PROVIDER_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
