package com.tastyhouse.application.review.service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.review.port.in.ShopReviewStatisticsQueryUseCase;
import com.tastyhouse.application.review.port.out.ShopReviewCategoryAverageResult;
import com.tastyhouse.application.review.port.out.ShopReviewMonthlyStatResult;
import com.tastyhouse.application.review.port.out.ShopReviewStatisticsOwnerResult;
import com.tastyhouse.application.review.port.out.ShopReviewStatisticsQueryPort;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional(readOnly = true)
class ShopReviewStatisticsQueryService implements ShopReviewStatisticsQueryUseCase {

    private static final ShopReviewStatisticsOwnerResult EMPTY_STATISTICS = new ShopReviewStatisticsOwnerResult(
        false, null, null, null, Map.of(), null, null, null, null, null, null, null, List.of()
    );

    private static final int STATISTICS_MONTHS = 6;

    private static final int DASHBOARD_GATE_DAYS = 180;

    private static final int RECENT_REVIEW_DAYS = 30;

    private final ShopReviewStatisticsQueryPort shopReviewStatisticsQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopReviewStatisticsQueryService(
        ShopReviewStatisticsQueryPort shopReviewStatisticsQueryPort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopReviewStatisticsQueryPort = shopReviewStatisticsQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public ShopReviewStatisticsOwnerResult getStatistics(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime gateFrom = now.minusDays(DASHBOARD_GATE_DAYS);
        if (shopReviewStatisticsQueryPort.countSince(shopId, gateFrom) == 0) {
            return EMPTY_STATISTICS;
        }

        YearMonth currentMonth = YearMonth.from(now);
        YearMonth firstMonth = currentMonth.minusMonths(STATISTICS_MONTHS - 1L);
        LocalDateTime periodFrom = firstMonth.atDay(1).atStartOfDay();
        LocalDateTime periodTo = currentMonth.plusMonths(1).atDay(1).atStartOfDay();

        long totalReviewCount = shopReviewStatisticsQueryPort.countBetween(shopId, periodFrom, periodTo);
        long willRevisitCount =
            shopReviewStatisticsQueryPort.countWillRevisitBetween(shopId, periodFrom, periodTo);
        ShopReviewCategoryAverageResult averages =
            shopReviewStatisticsQueryPort.getCategoryAverages(shopId, periodFrom, periodTo);

        return new ShopReviewStatisticsOwnerResult(
            true,
            roundToTenth(shopReviewStatisticsQueryPort.getAverageTotalRating(shopId, periodFrom, periodTo)),
            totalReviewCount,
            shopReviewStatisticsQueryPort.countSince(shopId, now.minusDays(RECENT_REVIEW_DAYS)),
            normalizeRatingCounts(shopReviewStatisticsQueryPort.getRatingCounts(shopId, periodFrom, periodTo)),
            roundToTenth(averages.tasteRating()),
            roundToTenth(averages.amountRating()),
            roundToTenth(averages.priceRating()),
            roundToTenth(averages.atmosphereRating()),
            roundToTenth(averages.kindnessRating()),
            roundToTenth(averages.hygieneRating()),
            toPercentage(willRevisitCount, totalReviewCount),
            toMonthlyStats(shopId, firstMonth, periodFrom, periodTo)
        );
    }

    private List<ShopReviewMonthlyStatResult> toMonthlyStats(
        Long shopId,
        YearMonth firstMonth,
        LocalDateTime periodFrom,
        LocalDateTime periodTo
    ) {
        Map<String, Long> counts = shopReviewStatisticsQueryPort.getMonthlyReviewCounts(shopId, periodFrom, periodTo);
        Map<String, Double> averages =
            shopReviewStatisticsQueryPort.getMonthlyAverageRatings(shopId, periodFrom, periodTo);

        List<ShopReviewMonthlyStatResult> monthlyStats = new java.util.ArrayList<>(STATISTICS_MONTHS);
        for (int offset = 0; offset < STATISTICS_MONTHS; offset++) {
            YearMonth month = firstMonth.plusMonths(offset);
            String key = month.toString();
            monthlyStats.add(new ShopReviewMonthlyStatResult(
                key,
                roundToTenth(averages.get(key)),
                counts.getOrDefault(key, 0L)
            ));
        }
        return monthlyStats;
    }

    private Map<Integer, Long> normalizeRatingCounts(Map<Integer, Long> ratingCounts) {
        Map<Integer, Long> normalized = new LinkedHashMap<>();
        for (int rating = 1; rating <= 5; rating++) {
            normalized.put(rating, ratingCounts.getOrDefault(rating, 0L));
        }
        return normalized;
    }

    private Double toPercentage(long count, long total) {
        if (total == 0) {
            return null;
        }
        return roundToTenth((double) count * 100 / total);
    }

    private Double roundToTenth(Double value) {
        return value == null ? null : Math.round(value * 10) / 10.0;
    }
}
