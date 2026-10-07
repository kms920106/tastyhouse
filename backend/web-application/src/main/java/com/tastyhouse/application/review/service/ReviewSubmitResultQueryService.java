package com.tastyhouse.application.review.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.application.review.port.in.ReviewSubmitResultQueryUseCase;
import com.tastyhouse.application.review.port.out.ReviewDetailResult;
import com.tastyhouse.application.review.port.out.ReviewSubmitResultView;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional(readOnly = true)
class ReviewSubmitResultQueryService implements ReviewSubmitResultQueryUseCase {

    private final ReviewDetailReader reviewDetailReader;

    public ReviewSubmitResultQueryService(ReviewDetailReader reviewDetailReader) {
        this.reviewDetailReader = reviewDetailReader;
    }

    @Override
    public ReviewSubmitResultView getReviewSubmitResult(Long reviewId, Long authorMemberId) {
        ReviewDetailResult detail = reviewDetailReader.findReviewDetailResult(ReviewId.of(reviewId), authorMemberId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.REVIEW_NOT_FOUND));

        return new ReviewSubmitResultView(
            detail.id(),
            reviewDetailReader.findProductIdOfReview(reviewId),
            detail.tasteRating(),
            detail.amountRating(),
            detail.priceRating(),
            detail.totalRating(),
            detail.content(),
            detail.imageUrls(),
            detail.tagNames(),
            detail.createdAt()
        );
    }
}
