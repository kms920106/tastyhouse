package com.tastyhouse.application.review.port.in;

import com.tastyhouse.application.review.port.out.ShopReviewDetailViewResult;

public interface ShopReviewDetailQueryUseCase {

    ShopReviewDetailViewResult getReviewDetail(Long ceoId, Long shopId, Long reviewId);
}
