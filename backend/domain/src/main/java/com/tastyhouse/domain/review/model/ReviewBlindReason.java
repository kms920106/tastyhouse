package com.tastyhouse.domain.review.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public enum ReviewBlindReason {
    ADVERTISEMENT("광고·홍보"),
    PROFANITY("욕설·비방"),
    IRRELEVANT("주문과 무관"),
    PRIVACY("개인정보 노출"),
    ETC("기타");

    private final String description;

    ReviewBlindReason(String description) {
        this.description = description;
    }

    public static ReviewBlindReason from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.REVIEW_BLIND_REASON_UNKNOWN,
                ErrorCode.REVIEW_BLIND_REASON_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }

    public String getDescription() {
        return this.description;
    }
}
