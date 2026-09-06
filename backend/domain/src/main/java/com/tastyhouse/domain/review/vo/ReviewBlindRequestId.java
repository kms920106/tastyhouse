package com.tastyhouse.domain.review.vo;

/**
 * 리뷰 게시중단 요청 식별자.
 */
public record ReviewBlindRequestId(Long value) {

    public ReviewBlindRequestId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("ReviewBlindRequestId는 양수여야 합니다: " + value);
        }
    }

    public static ReviewBlindRequestId of(Long value) {
        return new ReviewBlindRequestId(value);
    }
}
