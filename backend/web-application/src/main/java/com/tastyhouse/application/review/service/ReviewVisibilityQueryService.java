package com.tastyhouse.application.review.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.review.port.in.ReviewVisibilityQueryUseCase;

@Service
@Transactional(readOnly = true)
class ReviewVisibilityQueryService implements ReviewVisibilityQueryUseCase {

    private final ReviewDetailReader reviewDetailReader;

    public ReviewVisibilityQueryService(ReviewDetailReader reviewDetailReader) {
        this.reviewDetailReader = reviewDetailReader;
    }

    @Override
    public void requireVisibleReview(Long reviewId, Long viewerMemberId) {
        reviewDetailReader.requireVisibleReview(reviewId, viewerMemberId);
    }
}
