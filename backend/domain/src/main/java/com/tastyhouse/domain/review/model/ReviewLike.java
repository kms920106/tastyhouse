package com.tastyhouse.domain.review.model;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.review.vo.ReviewId;

public class ReviewLike {
    private final Long id;
    private final ReviewId reviewId;
    private final MemberId memberId;

    private ReviewLike(Long id, ReviewId reviewId, MemberId memberId) {
        this.id = id;
        this.reviewId = reviewId;
        this.memberId = memberId;
    }

    public static ReviewLike of(ReviewId reviewId, MemberId memberId) {
        return new ReviewLike(null, reviewId, memberId);
    }

    public static ReviewLike reconstitute(Long id, ReviewId reviewId, MemberId memberId) {
        return new ReviewLike(id, reviewId, memberId);
    }

    public Long getId() {
        return this.id;
    }

    public ReviewId getReviewId() {
        return this.reviewId;
    }

    public MemberId getMemberId() {
        return this.memberId;
    }
}
