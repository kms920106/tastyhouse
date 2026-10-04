package com.tastyhouse.domain.review.model;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;

public enum ReviewListTab {

    ALL,
    UNANSWERED,
    BLINDED,
    OWNER_ONLY;

    public static ReviewListTab from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new DomainException(DomainErrorCode.REVIEW_TAB_UNKNOWN,
                DomainErrorCode.REVIEW_TAB_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
