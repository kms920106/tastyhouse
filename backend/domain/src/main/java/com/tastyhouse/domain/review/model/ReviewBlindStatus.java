package com.tastyhouse.domain.review.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public enum ReviewBlindStatus {
    PENDING("대기"),
    APPROVED("게시중단"),
    REJECTED("반려"),
    CANCELED("취소"),

    EXPIRED("재노출"),

    DELETED("삭제");

    private final String description;

    ReviewBlindStatus(String description) {
        this.description = description;
    }

    public static ReviewBlindStatus from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.REVIEW_BLIND_STATUS_UNKNOWN,
                ErrorCode.REVIEW_BLIND_STATUS_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }

    public String getDescription() {
        return this.description;
    }
}
