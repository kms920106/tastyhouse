package com.tastyhouse.domain.review.model;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;

public enum ReviewSortType {

    RECOMMENDED,
    LATEST,
    OLDEST;

    public static ReviewSortType from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new DomainException(DomainErrorCode.REVIEW_SORT_TYPE_UNKNOWN,
                DomainErrorCode.REVIEW_SORT_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
