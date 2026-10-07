package com.tastyhouse.application.review.port.in;

import com.tastyhouse.application.review.port.out.ReviewBlindRequestDetailResult;

public interface ReviewBlindRequestDetailQueryUseCase {

    ReviewBlindRequestDetailResult getBlindRequest(Long id);
}
