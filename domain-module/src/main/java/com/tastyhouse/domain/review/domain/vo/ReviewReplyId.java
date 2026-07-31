package com.tastyhouse.domain.review.domain.vo;

public record ReviewReplyId(Long value) {

    public ReviewReplyId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("ReviewReplyId는 양수여야 합니다: " + value);
        }
    }

    public static ReviewReplyId of(Long value) {
        return new ReviewReplyId(value);
    }
}
