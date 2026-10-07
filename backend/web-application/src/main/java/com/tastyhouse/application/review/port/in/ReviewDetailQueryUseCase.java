package com.tastyhouse.application.review.port.in;

import java.util.Optional;

import com.tastyhouse.application.review.port.out.ReviewDetailView;

public interface ReviewDetailQueryUseCase {

    Optional<ReviewDetailView> findReviewDetail(Long reviewId, Long viewerMemberId);
}
