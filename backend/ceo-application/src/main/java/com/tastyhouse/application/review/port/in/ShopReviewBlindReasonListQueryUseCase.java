package com.tastyhouse.application.review.port.in;

import java.util.List;

import com.tastyhouse.application.review.port.out.ReviewBlindReasonView;

public interface ShopReviewBlindReasonListQueryUseCase {

    List<ReviewBlindReasonView> getBlindReasons();
}
