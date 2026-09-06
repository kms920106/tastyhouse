package com.tastyhouse.domain.review.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public enum ReviewSortType {
    RECOMMENDED,
    LATEST,
    OLDEST;

    public static ReviewSortType from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.REVIEW_SORT_TYPE_UNKNOWN,
                ErrorCode.REVIEW_SORT_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
