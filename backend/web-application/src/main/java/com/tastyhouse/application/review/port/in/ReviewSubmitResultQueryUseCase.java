package com.tastyhouse.application.review.port.in;

import com.tastyhouse.application.review.port.out.ReviewSubmitResultView;

public interface ReviewSubmitResultQueryUseCase {

    ReviewSubmitResultView getReviewSubmitResult(Long reviewId, Long authorMemberId);
}
