package com.tastyhouse.domain.review.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 리뷰 게시중단 요청 사유.
 *
 * <p>{@link #ETC}는 상세 사유가 필수다 — 그 판정은 사유 자체가 아니라 요청 생성 불변식이므로
 * {@code ReviewBlindRequestService}가 소유한다(enum이 다른 필드의 존재 여부를 알지 않는다).
 */
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
