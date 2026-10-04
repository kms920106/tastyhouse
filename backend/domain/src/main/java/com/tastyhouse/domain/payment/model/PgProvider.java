package com.tastyhouse.domain.payment.model;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;

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
            throw new DomainException(DomainErrorCode.PG_PROVIDER_UNKNOWN,
                DomainErrorCode.PG_PROVIDER_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
