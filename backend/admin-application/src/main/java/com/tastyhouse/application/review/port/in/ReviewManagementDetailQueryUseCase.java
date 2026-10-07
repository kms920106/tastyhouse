package com.tastyhouse.application.review.port.in;

import com.tastyhouse.application.review.port.out.ReviewManagementDetailResult;

public interface ReviewManagementDetailQueryUseCase {

    ReviewManagementDetailResult getReview(Long id);
}
