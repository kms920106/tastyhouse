package com.tastyhouse.application.review.port.in;

import java.util.Optional;

import com.tastyhouse.application.review.port.out.ReviewProductView;

public interface ReviewProductQueryUseCase {

    Optional<ReviewProductView> findReviewProduct(Long reviewId, Long viewerMemberId);
}
