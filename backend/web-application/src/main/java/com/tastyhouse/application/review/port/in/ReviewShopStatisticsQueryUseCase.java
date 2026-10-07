package com.tastyhouse.application.review.port.in;

import com.tastyhouse.application.review.port.out.ShopReviewStatisticsResult;

public interface ReviewShopStatisticsQueryUseCase {

    ShopReviewStatisticsResult findShopReviewStatistics(Long shopId);
}
