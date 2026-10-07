package com.tastyhouse.application.review.port.in;

public interface ReviewVisibilityQueryUseCase {

    void requireVisibleReview(Long reviewId, Long viewerMemberId);
}
