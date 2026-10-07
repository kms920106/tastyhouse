package com.tastyhouse.application.review.service;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.review.port.in.ReviewShopStatisticsQueryUseCase;
import com.tastyhouse.application.review.port.out.ReviewStatisticsQueryPort;
import com.tastyhouse.application.review.port.out.ShopReviewStatisticsResult;

@Service
@Transactional(readOnly = true)
class ReviewShopStatisticsQueryService implements ReviewShopStatisticsQueryUseCase {

    private final ReviewStatisticsQueryPort reviewStatisticsQueryPort;

    public ReviewShopStatisticsQueryService(ReviewStatisticsQueryPort reviewStatisticsQueryPort) {
        this.reviewStatisticsQueryPort = reviewStatisticsQueryPort;
    }

    @Override
    public ShopReviewStatisticsResult findShopReviewStatistics(Long shopId) {
        Long totalCount = reviewStatisticsQueryPort.countVisibleByShopId(shopId);

        Map<Integer, Long> ratingMap = reviewStatisticsQueryPort.getRatingCounts(shopId);
        for (int rating = 1; rating <= 5; rating++) {
            ratingMap.putIfAbsent(rating, 0L);
        }

        if (totalCount > 0) {
            Long willRevisitCount = reviewStatisticsQueryPort.countWillRevisit(shopId);
            double willRevisitPercentage = (willRevisitCount * 100.0) / totalCount;

            int currentYear = LocalDateTime.now().getYear();
            Map<Integer, Long> monthlyMap = reviewStatisticsQueryPort.getMonthlyReviewCounts(shopId, currentYear);

            return new ShopReviewStatisticsResult(
                totalCount,
                reviewStatisticsQueryPort.getAverageTasteRating(shopId),
                reviewStatisticsQueryPort.getAverageAmountRating(shopId),
                reviewStatisticsQueryPort.getAveragePriceRating(shopId),
                reviewStatisticsQueryPort.getAverageAtmosphereRating(shopId),
                reviewStatisticsQueryPort.getAverageKindnessRating(shopId),
                reviewStatisticsQueryPort.getAverageHygieneRating(shopId),
                willRevisitPercentage,
                ratingMap,
                monthlyMap
            );
        }

        return new ShopReviewStatisticsResult(
            totalCount,
            null, null, null, null, null, null, null,
            ratingMap,
            null
        );
    }
}
