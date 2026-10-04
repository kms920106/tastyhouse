package com.tastyhouse.domain.ceo.model;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;

public enum CeoLoginResult {

    SUCCESS("로그인 성공"),
    FAILURE("로그인 실패");

    private final String description;

    CeoLoginResult(String description) {
        this.description = description;
    }

    public static CeoLoginResult from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new DomainException(DomainErrorCode.CEO_LOGIN_RESULT_UNKNOWN,
                DomainErrorCode.CEO_LOGIN_RESULT_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }

    public String getDescription() {
        return this.description;
    }
}
