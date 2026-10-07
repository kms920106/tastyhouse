package com.tastyhouse.application.review.port.in;

import com.tastyhouse.application.review.port.out.ShopReviewStatisticsOwnerResult;

public interface ShopReviewStatisticsQueryUseCase {

    ShopReviewStatisticsOwnerResult getStatistics(Long ceoId, Long shopId);
}
