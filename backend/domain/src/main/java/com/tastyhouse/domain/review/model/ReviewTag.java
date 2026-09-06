package com.tastyhouse.domain.review.model;

import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.shop.vo.TagId;

public class ReviewTag {
    private final Long id;
    private final ReviewId reviewId;
    private final TagId tagId;

    private ReviewTag(Long id, ReviewId reviewId, TagId tagId) {
        this.id = id;
        this.reviewId = reviewId;
        this.tagId = tagId;
    }

    public static ReviewTag of(ReviewId reviewId, TagId tagId) {
        return new ReviewTag(null, reviewId, tagId);
    }

    public static ReviewTag reconstitute(Long id, ReviewId reviewId, TagId tagId) {
        return new ReviewTag(id, reviewId, tagId);
    }

    public Long getId() {
        return this.id;
    }

    public ReviewId getReviewId() {
        return this.reviewId;
    }

    public TagId getTagId() {
        return this.tagId;
    }
}
