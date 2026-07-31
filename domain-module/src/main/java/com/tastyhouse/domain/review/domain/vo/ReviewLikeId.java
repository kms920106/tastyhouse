package com.tastyhouse.domain.review.domain.vo;

public record ReviewLikeId(Long value) {

    public ReviewLikeId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("ReviewLikeId는 양수여야 합니다: " + value);
        }
    }

    public static ReviewLikeId of(Long value) {
        return new ReviewLikeId(value);
    }
}
