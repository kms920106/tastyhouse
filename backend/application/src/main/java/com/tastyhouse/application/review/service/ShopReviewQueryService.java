package com.tastyhouse.application.review.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.review.port.in.ShopReviewQueryUseCase;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.review.model.ReviewBlindReason;
import com.tastyhouse.domain.review.model.ReviewListTab;
import com.tastyhouse.domain.review.model.ReviewOwnerReply;
import com.tastyhouse.domain.review.model.ReviewSortType;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.review.port.out.ReviewBlindReasonView;
import com.tastyhouse.application.review.port.out.ShopReviewStatisticsQueryPort;
import com.tastyhouse.application.review.port.out.ShopReviewCategoryAverageResult;
import com.tastyhouse.application.review.port.out.ShopReviewDisplaySettingOwnerQueryPort;
import com.tastyhouse.application.review.port.out.ShopReviewManagementDetailResult;
import com.tastyhouse.application.review.port.out.ShopReviewManagementListItemResult;
import com.tastyhouse.application.review.port.out.ShopReviewManagementQueryPort;
import com.tastyhouse.application.review.port.out.ShopReviewManagementSearchCondition;
import com.tastyhouse.application.review.port.out.ShopReviewDetailViewResult;
import com.tastyhouse.application.review.port.out.ShopReviewListItemViewResult;
import com.tastyhouse.application.review.port.out.ShopReviewMonthlyStatResult;
import com.tastyhouse.application.review.port.out.ShopReviewReplyWindow;
import com.tastyhouse.application.review.port.out.ShopReviewSortTypeResult;
import com.tastyhouse.application.review.port.out.ShopReviewSortTypeView;
import com.tastyhouse.application.review.port.out.ShopReviewStatisticsOwnerResult;

@Service
@CeoApp
@Transactional(readOnly = true)
public class ShopReviewQueryService implements ShopReviewQueryUseCase {

    private static final ShopReviewStatisticsOwnerResult EMPTY_STATISTICS = new ShopReviewStatisticsOwnerResult(
        false, null, null, null, Map.of(), null, null, null, null, null, null, null, List.of()
    );

    private static final int STATISTICS_MONTHS = 6;

    private static final int DASHBOARD_GATE_DAYS = 180;

    private static final int RECENT_REVIEW_DAYS = 30;

    private final ShopReviewManagementQueryPort shopReviewManagementQueryPort;
    private final ShopReviewStatisticsQueryPort shopReviewStatisticsQueryPort;
    private final ShopReviewDisplaySettingOwnerQueryPort shopReviewDisplaySettingOwnerQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopReviewQueryService(
        ShopReviewManagementQueryPort shopReviewManagementQueryPort,
        ShopReviewStatisticsQueryPort shopReviewStatisticsQueryPort,
        ShopReviewDisplaySettingOwnerQueryPort shopReviewDisplaySettingOwnerQueryPort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopReviewManagementQueryPort = shopReviewManagementQueryPort;
        this.shopReviewStatisticsQueryPort = shopReviewStatisticsQueryPort;
        this.shopReviewDisplaySettingOwnerQueryPort = shopReviewDisplaySettingOwnerQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public PageResult<ShopReviewListItemViewResult> getReviews(
        Long ceoId,
        Long shopId,
        String tab,
        LocalDate startDate,
        LocalDate endDate,
        Integer rating,
        String orderMethod,
        Boolean hasImage,
        String sortType,
        int page,
        int size
    ) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        validateDateRange(startDate, endDate);

        ReviewListTab tabFilter = tab == null ? ReviewListTab.ALL : ReviewListTab.from(tab);
        OrderMethod orderMethodFilter = orderMethod == null ? null : OrderMethod.from(orderMethod);

        ShopReviewManagementSearchCondition condition = ShopReviewManagementSearchCondition.of(
            shopId,
            tabFilter,
            startDate,
            endDate,
            rating,
            orderMethodFilter,
            hasImage,
            resolveSortType(shopId, sortType)
        );
        PageQuery pageQuery = PageQuery.of(page, size);

        return shopReviewManagementQueryPort.findShopReviews(condition, pageQuery)

            .map(this::toListItemViewResult);
    }

    @Override
    public ShopReviewDetailViewResult getReviewDetail(Long ceoId, Long shopId, Long reviewId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopReviewManagementDetailResult detail =
            shopReviewManagementQueryPort.findShopReviewDetail(ReviewId.of(reviewId))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_NOT_FOUND));
        if (!shopId.equals(detail.shopId())) {
            throw new BusinessException(ErrorCode.SHOP_ACCESS_DENIED);
        }

        return toDetailViewResult(detail);
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

    @Override
    public ShopReviewSortTypeView getSortType(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        return shopReviewDisplaySettingOwnerQueryPort.findSortTypeSettingByShopId(shopId)
            .map(this::toSortTypeView)
            .orElseGet(() -> toSortTypeView(new ShopReviewSortTypeResult(ReviewSortType.LATEST, null)));
    }

    @Override
    public List<ReviewBlindReasonView> getBlindReasons() {
        return Arrays.stream(ReviewBlindReason.values())
            .map(reason -> new ReviewBlindReasonView(reason.name(), reason.getDescription()))
            .toList();
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

    private ReviewSortType resolveSortType(Long shopId, String sortType) {
        if (sortType != null) {
            return ReviewSortType.from(sortType);
        }
        return shopReviewDisplaySettingOwnerQueryPort.findSortTypeByShopId(shopId)
            .orElse(ReviewSortType.LATEST);
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BusinessException(ErrorCode.REVIEW_DATE_RANGE_INVALID);
        }
    }

    private ShopReviewListItemViewResult toListItemViewResult(ShopReviewManagementListItemResult result) {
        return new ShopReviewListItemViewResult(result, toReplyWindow(result.createdAt()));
    }

    private ShopReviewDetailViewResult toDetailViewResult(ShopReviewManagementDetailResult result) {
        return new ShopReviewDetailViewResult(result, toReplyWindow(result.createdAt()));
    }

    private ShopReviewReplyWindow toReplyWindow(LocalDateTime reviewCreatedAt) {
        LocalDate replyDeadline = reviewCreatedAt.toLocalDate().plusDays(ReviewOwnerReply.REPLY_PERIOD_DAYS);
        return new ShopReviewReplyWindow(replyDeadline, !LocalDate.now().isAfter(replyDeadline));
    }

    private ShopReviewSortTypeView toSortTypeView(ShopReviewSortTypeResult result) {
        ReviewSortType sortType = result.sortType();
        return new ShopReviewSortTypeView(sortType.name(), describeSortType(sortType), result.updatedAt());
    }

    private String describeSortType(ReviewSortType sortType) {
        return switch (sortType) {
            case RECOMMENDED -> "추천순";
            case LATEST -> "최신순";
            case OLDEST -> "등록순";
        };
    }
}
