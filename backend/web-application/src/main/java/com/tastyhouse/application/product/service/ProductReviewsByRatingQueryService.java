package com.tastyhouse.application.product.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.review.model.ReviewSortType;
import com.tastyhouse.application.product.port.in.ProductReviewsByRatingQueryUseCase;
import com.tastyhouse.application.review.port.out.LatestReviewListItemResult;
import com.tastyhouse.application.review.port.out.ReviewFeedQueryPort;
import com.tastyhouse.application.review.port.out.ReviewStatisticsQueryPort;
import com.tastyhouse.application.review.port.out.ReviewsByRatingResult;
import com.tastyhouse.application.review.service.ReviewSortSpecs;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@Service
@Transactional(readOnly = true)
class ProductReviewsByRatingQueryService implements ProductReviewsByRatingQueryUseCase {

    private final ReviewFeedQueryPort reviewFeedQueryPort;
    private final ReviewStatisticsQueryPort reviewStatisticsQueryPort;

    public ProductReviewsByRatingQueryService(
        ReviewFeedQueryPort reviewFeedQueryPort,
        ReviewStatisticsQueryPort reviewStatisticsQueryPort
    ) {
        this.reviewFeedQueryPort = reviewFeedQueryPort;
        this.reviewStatisticsQueryPort = reviewStatisticsQueryPort;
    }

    @Override
    public ReviewsByRatingResult getProductReviewsByRatingWithPagination(
        Long productId,
        int page,
        int size,
        Boolean hasImage
    ) {
        return findProductReviewsByRating(productId, page, size, hasImage);
    }

    private ReviewsByRatingResult findProductReviewsByRating(Long productId, int page, int size, Boolean hasImage) {
        Map<Integer, List<LatestReviewListItemResult>> reviewsByRating = new HashMap<>();
        for (int rating = 1; rating <= 5; rating++) {
            reviewsByRating.put(rating, reviewFeedQueryPort.findReviewsByProductIdAndRating(productId, rating, 5));
        }

        PageQuery pageQuery = PageQuery.of(page, size);
        PageResult<LatestReviewListItemResult> allReviewsPage =
            reviewFeedQueryPort.findLatestReviewsByProductId(
                productId,
                null,
                pageQuery,
                hasImage,
                ReviewSortSpecs.of(ReviewSortType.LATEST)
            );

        Long totalReviewCount = reviewStatisticsQueryPort.countVisibleByProductId(productId);

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
}
