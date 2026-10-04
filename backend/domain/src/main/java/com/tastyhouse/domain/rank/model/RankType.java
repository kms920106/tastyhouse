package com.tastyhouse.domain.rank.model;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;

public enum RankType {

    ALL,
    MONTHLY,
    WEEKLY
    ;

    public static RankType from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new DomainException(DomainErrorCode.RANK_TYPE_UNKNOWN,
                DomainErrorCode.RANK_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
