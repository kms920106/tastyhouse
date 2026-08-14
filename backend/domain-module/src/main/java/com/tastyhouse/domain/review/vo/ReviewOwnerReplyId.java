package com.tastyhouse.domain.review.vo;

/**
 * 사장님 답변 식별자.
 */
public record ReviewOwnerReplyId(Long value) {

    public ReviewOwnerReplyId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("ReviewOwnerReplyId는 양수여야 합니다: " + value);
        }
    }

    public static ReviewOwnerReplyId of(Long value) {
        return new ReviewOwnerReplyId(value);
    }
}
