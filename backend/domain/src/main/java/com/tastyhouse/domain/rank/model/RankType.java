package com.tastyhouse.domain.rank.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public enum RankType {
    ALL,
    MONTHLY,
    WEEKLY
    ;

    public static RankType from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.RANK_TYPE_UNKNOWN,
                ErrorCode.RANK_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
