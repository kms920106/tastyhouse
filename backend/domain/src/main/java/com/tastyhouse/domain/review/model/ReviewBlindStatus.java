package com.tastyhouse.domain.review.model;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;

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
            throw new DomainException(DomainErrorCode.REVIEW_BLIND_STATUS_UNKNOWN,
                DomainErrorCode.REVIEW_BLIND_STATUS_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }

    public String getDescription() {
        return this.description;
    }
}
