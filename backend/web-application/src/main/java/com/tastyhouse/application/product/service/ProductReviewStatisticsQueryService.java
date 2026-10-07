package com.tastyhouse.application.product.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.in.ProductReviewStatisticsQueryUseCase;
import com.tastyhouse.application.product.port.out.ProductDetailResult;
import com.tastyhouse.application.product.port.out.ProductReviewStatisticsView;
import com.tastyhouse.application.review.port.out.ProductReviewStatisticsResult;
import com.tastyhouse.application.review.port.out.ReviewStatisticsQueryPort;

@Service
@Transactional(readOnly = true)
class ProductReviewStatisticsQueryService implements ProductReviewStatisticsQueryUseCase {

    private final ReviewStatisticsQueryPort reviewStatisticsQueryPort;
    private final ProductDetailReader productDetailReader;

    public ProductReviewStatisticsQueryService(
        ReviewStatisticsQueryPort reviewStatisticsQueryPort,
        ProductDetailReader productDetailReader
    ) {
        this.reviewStatisticsQueryPort = reviewStatisticsQueryPort;
        this.productDetailReader = productDetailReader;
    }

    @Override
    public ProductReviewStatisticsView getProductReviewStatistics(Long productId) {
        ProductReviewStatisticsResult statistics = findProductReviewStatistics(productId);
        ProductDetailResult product = productDetailReader.read(productId);

        return new ProductReviewStatisticsView(
            product.rating(),
            statistics.totalReviewCount(),
            statistics.averageTasteRating(),
            statistics.averageAmountRating(),
            statistics.averagePriceRating()
        );
    }

    private ProductReviewStatisticsResult findProductReviewStatistics(Long productId) {
        Long totalCount = reviewStatisticsQueryPort.countVisibleByProductId(productId);

        if (totalCount > 0) {
            return new ProductReviewStatisticsResult(
                totalCount,
                reviewStatisticsQueryPort.getAverageTasteRatingByProductId(productId),
                reviewStatisticsQueryPort.getAverageAmountRatingByProductId(productId),
                reviewStatisticsQueryPort.getAveragePriceRatingByProductId(productId)
            );
        }

        return new ProductReviewStatisticsResult(totalCount, null, null, null);
    }
}
