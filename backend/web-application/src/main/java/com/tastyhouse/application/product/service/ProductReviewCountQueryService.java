package com.tastyhouse.application.product.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.in.ProductReviewCountQueryUseCase;
import com.tastyhouse.application.review.port.out.ProductReviewStatisticsResult;
import com.tastyhouse.application.review.port.out.ReviewStatisticsQueryPort;

@Service
@Transactional(readOnly = true)
class ProductReviewCountQueryService implements ProductReviewCountQueryUseCase {

    private final ReviewStatisticsQueryPort reviewStatisticsQueryPort;
    private final ProductDetailReader productDetailReader;

    public ProductReviewCountQueryService(
        ReviewStatisticsQueryPort reviewStatisticsQueryPort,
        ProductDetailReader productDetailReader
    ) {
        this.reviewStatisticsQueryPort = reviewStatisticsQueryPort;
        this.productDetailReader = productDetailReader;
    }

    @Override
    public int findProductReviewCount(Long productId) {
        productDetailReader.read(productId);
        ProductReviewStatisticsResult statistics = findProductReviewStatistics(productId);
        Long total = statistics.totalReviewCount();
        return total != null ? total.intValue() : 0;
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
