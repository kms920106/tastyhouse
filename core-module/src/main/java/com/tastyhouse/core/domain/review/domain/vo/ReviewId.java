package com.tastyhouse.core.domain.review.domain.vo;

public record ReviewId(Long value) {

    public ReviewId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("ReviewId는 양수여야 합니다: " + value);
        }
    }
}
