package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.review.port.in.ReviewShopStatisticsQueryUseCase;
import com.tastyhouse.application.review.port.out.ShopReviewStatisticsResult;
import com.tastyhouse.application.shop.port.in.ShopReviewStatisticsQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopReviewStatisticsViewResult;
import com.tastyhouse.application.shop.port.out.ShopVisibleDetailResult;

@Service
@Transactional(readOnly = true)
class ShopReviewStatisticsQueryService implements ShopReviewStatisticsQueryUseCase {

    private final ShopVisibleReader shopVisibleReader;
    private final ReviewShopStatisticsQueryUseCase reviewShopStatisticsQueryUseCase;

    public ShopReviewStatisticsQueryService(
        ShopVisibleReader shopVisibleReader,
        ReviewShopStatisticsQueryUseCase reviewShopStatisticsQueryUseCase
    ) {
        this.shopVisibleReader = shopVisibleReader;
        this.reviewShopStatisticsQueryUseCase = reviewShopStatisticsQueryUseCase;
    }

    @Override
    public ShopReviewStatisticsViewResult getShopReviewStatistics(Long shopId) {
        ShopReviewStatisticsResult statistics = reviewShopStatisticsQueryUseCase.findShopReviewStatistics(shopId);

        ShopVisibleDetailResult shop = shopVisibleReader.findVisibleShop(shopId);

        return new ShopReviewStatisticsViewResult(
            shop.rating(),
            statistics.totalReviewCount(),
            statistics.averageTasteRating(),
            statistics.averageAmountRating(),
            statistics.averagePriceRating(),
            statistics.averageAtmosphereRating(),
            statistics.averageKindnessRating(),
            statistics.averageHygieneRating(),
            statistics.willRevisitPercentage(),
            statistics.monthlyReviewCounts(),
            statistics.ratingCounts()
        );
    }
}
