package com.tastyhouse.domain.review.vo;

public record ReviewId(Long value) {

    public ReviewId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("ReviewId는 양수여야 합니다: " + value);
        }
    }

    public static ReviewId of(Long value) {
        return new ReviewId(value);
    }
}
