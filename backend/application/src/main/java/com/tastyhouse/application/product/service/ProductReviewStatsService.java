package com.tastyhouse.application.product.service;

import com.tastyhouse.application.product.port.out.ProductReviewStatisticsPort;
import com.tastyhouse.application.product.store.ProductRepository;
import com.tastyhouse.domain.product.vo.ProductId;

public class ProductReviewStatsService {
    private final ProductRepository productRepository;
    private final ProductReviewStatisticsPort productReviewStatisticsPort;

    public ProductReviewStatsService(
        ProductRepository productRepository,
        ProductReviewStatisticsPort productReviewStatisticsPort
    ) {
        this.productRepository = productRepository;
        this.productReviewStatisticsPort = productReviewStatisticsPort;
    }

    public void updateReviewStats(Long productId) {
        productRepository.findById(ProductId.of(productId)).ifPresent(product -> {
            Long count = productReviewStatisticsPort.countVisibleMenuReviewsByProductId(productId);
            Double rating = roundToTenth(productReviewStatisticsPort.getAverageMenuRatingByProductId(productId));
            product.updateReviewStats(rating, count != null ? count.intValue() : 0);
            productRepository.save(product);
        });
    }

    private Double roundToTenth(Double rating) {
        return rating == null ? null : Math.round(rating * 10.0) / 10.0;
    }
}
