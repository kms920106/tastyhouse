package com.tastyhouse.domain.payment.domain.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public enum PgProvider {
    TOSS,           // 토스페이먼츠
    KAKAO,          // 카카오페이
    NICE,           // 나이스페이
    KG_INICIS,      // KG이니시스
    NHN_KCP,        // NHN KCP
    SETTLE_BANK;    // 세틀뱅크

    public static PgProvider from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.PG_PROVIDER_UNKNOWN,
                ErrorCode.PG_PROVIDER_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
