package com.tastyhouse.application.review.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.application.review.port.out.ReviewDetailResult;
import com.tastyhouse.application.review.port.out.ReviewQueryPort;
import com.tastyhouse.application.review.port.out.ReviewTagQueryPort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Component
class ReviewDetailReader {

    private final ReviewQueryPort reviewQueryPort;
    private final ReviewTagQueryPort reviewTagQueryPort;

    public ReviewDetailReader(ReviewQueryPort reviewQueryPort, ReviewTagQueryPort reviewTagQueryPort) {
        this.reviewQueryPort = reviewQueryPort;
        this.reviewTagQueryPort = reviewTagQueryPort;
    }

    Optional<ReviewDetailResult> findReviewDetailResult(ReviewId reviewId, Long viewerMemberId) {
        return reviewQueryPort.findReviewDetail(reviewId.value(), viewerMemberId).map(result -> {
            List<Long> tagIds = reviewTagQueryPort.findTagIdsByReviewId(reviewId.value());
            if (tagIds.isEmpty()) {
                return result;
            }
            return result.withTagNames(reviewTagQueryPort.findTagNamesByIds(tagIds));
        });
    }

    Long findProductIdOfReview(Long reviewId) {
        return reviewQueryPort.findProductIdByReviewId(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.REVIEW_NOT_FOUND));
    }

    void requireVisibleReview(Long reviewId, Long viewerMemberId) {
        findReviewDetailResult(ReviewId.of(reviewId), viewerMemberId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.REVIEW_NOT_FOUND));
    }
}
