package com.tastyhouse.domain.review.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public enum ReviewListTab {
    ALL,
    UNANSWERED,
    BLINDED,
    OWNER_ONLY;

    public static ReviewListTab from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.REVIEW_TAB_UNKNOWN,
                ErrorCode.REVIEW_TAB_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
