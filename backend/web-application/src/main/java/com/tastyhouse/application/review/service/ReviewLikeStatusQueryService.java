package com.tastyhouse.application.review.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.application.review.port.in.ReviewLikeStatusQueryUseCase;
import com.tastyhouse.application.review.port.out.ReviewQueryPort;

@Service
@Transactional(readOnly = true)
class ReviewLikeStatusQueryService implements ReviewLikeStatusQueryUseCase {

    private final ReviewQueryPort reviewQueryPort;

    public ReviewLikeStatusQueryService(ReviewQueryPort reviewQueryPort) {
        this.reviewQueryPort = reviewQueryPort;
    }

    @Override
    public boolean isLiked(Long reviewId, Long memberId) {
        return reviewQueryPort.existsLike(ReviewId.of(reviewId).value(), memberId);
    }
}
