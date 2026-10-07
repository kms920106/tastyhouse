package com.tastyhouse.application.review.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.review.model.ReviewSortType;
import com.tastyhouse.application.review.port.in.ReviewShopByRatingQueryUseCase;
import com.tastyhouse.application.review.port.out.LatestReviewListItemResult;
import com.tastyhouse.application.review.port.out.ReviewQueryPort;
import com.tastyhouse.application.review.port.out.ReviewSortSpec;
import com.tastyhouse.application.review.port.out.ReviewStatisticsQueryPort;
import com.tastyhouse.application.review.port.out.ReviewsByRatingResult;
import com.tastyhouse.application.review.port.out.ShopReviewDisplaySettingQueryPort;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@Service
@Transactional(readOnly = true)
class ReviewShopByRatingQueryService implements ReviewShopByRatingQueryUseCase {

    private final ReviewQueryPort reviewQueryPort;
    private final ReviewStatisticsQueryPort reviewStatisticsQueryPort;
    private final ShopReviewDisplaySettingQueryPort shopReviewDisplaySettingQueryPort;

    public ReviewShopByRatingQueryService(
        ReviewQueryPort reviewQueryPort,
        ReviewStatisticsQueryPort reviewStatisticsQueryPort,
        ShopReviewDisplaySettingQueryPort shopReviewDisplaySettingQueryPort
    ) {
        this.reviewQueryPort = reviewQueryPort;
        this.reviewStatisticsQueryPort = reviewStatisticsQueryPort;
        this.shopReviewDisplaySettingQueryPort = shopReviewDisplaySettingQueryPort;
    }

    @Override
    public ReviewsByRatingResult findShopReviewsByRating(
        Long shopId,
        int page,
        int size,
        Boolean hasImage,
        String sortType
    ) {
        Map<Integer, List<LatestReviewListItemResult>> reviewsByRating = new HashMap<>();
        for (int rating = 1; rating <= 5; rating++) {
            reviewsByRating.put(rating, reviewQueryPort.findReviewsByShopIdAndRating(shopId, rating, 5));
        }

        PageQuery pageQuery = PageQuery.of(page, size);
        PageResult<LatestReviewListItemResult> allReviewsPage = reviewQueryPort.findLatestReviewsByShopId(
            shopId,
            null,
            pageQuery,
            hasImage,
            resolveSortType(shopId, sortType)
        );

        Long totalReviewCount = reviewStatisticsQueryPort.countVisibleByShopId(shopId);

        return new ReviewsByRatingResult(
            reviewsByRating,
            allReviewsPage.content(),
            totalReviewCount,
            allReviewsPage.totalElements(),
            allReviewsPage.totalPages(),
            allReviewsPage.page(),
            allReviewsPage.size()
        );
    }

    private ReviewSortSpec resolveSortType(Long shopId, String sortType) {
        if (sortType != null) {
            return ReviewSortSpecs.of(ReviewSortType.from(sortType));
        }
        ReviewSortType storedSortType = shopReviewDisplaySettingQueryPort.findSortTypeByShopId(shopId)
            .map(ReviewSortType::valueOf)
            .orElse(ReviewSortType.LATEST);
        return ReviewSortSpecs.of(storedSortType);
    }
}
