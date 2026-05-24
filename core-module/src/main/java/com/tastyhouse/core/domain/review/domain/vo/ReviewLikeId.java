package com.tastyhouse.core.domain.review.domain.vo;

public record ReviewLikeId(Long value) {

    public ReviewLikeId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("ReviewLikeId는 양수여야 합니다: " + value);
        }
    }
}
