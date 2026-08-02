package com.tastyhouse.domain.review.vo;

public record ReviewCommentId(Long value) {

    public ReviewCommentId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("ReviewCommentId는 양수여야 합니다: " + value);
        }
    }

    public static ReviewCommentId of(Long value) {
        return new ReviewCommentId(value);
    }
}
