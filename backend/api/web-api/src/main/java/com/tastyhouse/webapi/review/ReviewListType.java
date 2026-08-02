package com.tastyhouse.webapi.review;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public enum ReviewListType {
    ALL,
    FOLLOWING;

    public static ReviewListType from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.REVIEW_LIST_TYPE_UNKNOWN,
                ErrorCode.REVIEW_LIST_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
