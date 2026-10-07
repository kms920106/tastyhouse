package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shop.port.out.ShopReviewStatisticsViewResult;

public interface ShopReviewStatisticsQueryUseCase {

    ShopReviewStatisticsViewResult getShopReviewStatistics(Long shopId);
}
