package com.tastyhouse.application.product.service;

import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.application.product.port.out.ProductReviewStatisticsPort;
import com.tastyhouse.application.product.port.out.write.ProductPersistencePort;

public class ProductReviewStatsService {

    private final ProductPersistencePort productPersistencePort;
    private final ProductReviewStatisticsPort productReviewStatisticsPort;

    public ProductReviewStatsService(
        ProductPersistencePort productPersistencePort,
        ProductReviewStatisticsPort productReviewStatisticsPort
    ) {
        this.productPersistencePort = productPersistencePort;
        this.productReviewStatisticsPort = productReviewStatisticsPort;
    }

    public void updateReviewStats(Long productId) {
        productPersistencePort.findById(ProductId.of(productId)).ifPresent(product -> {
            Long count = productReviewStatisticsPort.countVisibleMenuReviewsByProductId(productId);
            Double rating = roundToTenth(productReviewStatisticsPort.getAverageMenuRatingByProductId(productId));
            product.updateReviewStats(rating, count != null ? count.intValue() : 0);
            productPersistencePort.save(product);
        });
    }

    private Double roundToTenth(Double rating) {
        return rating == null ? null : Math.round(rating * 10.0) / 10.0;
    }
}
