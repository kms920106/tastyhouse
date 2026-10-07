package com.tastyhouse.application.review.port.in;

import com.tastyhouse.application.review.port.out.ReviewWriteInfoView;

public interface ReviewWriteInfoQueryUseCase {

    ReviewWriteInfoView getReviewWriteInfo(Long orderProductId, Long memberId);
}
