package com.tastyhouse.domain.review.vo;

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
