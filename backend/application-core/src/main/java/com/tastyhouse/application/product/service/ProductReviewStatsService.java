package com.tastyhouse.application.product.service;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.application.product.port.out.ProductReviewStatisticsPort;
import com.tastyhouse.application.product.port.out.write.ProductLoadPort;
import com.tastyhouse.application.product.port.out.write.ProductSavePort;

@Service
public class ProductReviewStatsService {

    private final ProductLoadPort productLoadPort;
    private final ProductSavePort productSavePort;
    private final ProductReviewStatisticsPort productReviewStatisticsPort;

    public ProductReviewStatsService(
        ProductLoadPort productLoadPort,
        ProductSavePort productSavePort,
        ProductReviewStatisticsPort productReviewStatisticsPort
    ) {
        this.productLoadPort = productLoadPort;
        this.productSavePort = productSavePort;
        this.productReviewStatisticsPort = productReviewStatisticsPort;
    }

    public void updateReviewStats(Long productId) {
        productLoadPort.findActiveById(ProductId.of(productId)).ifPresent(product -> {
            Long count = productReviewStatisticsPort.countVisibleMenuReviewsByProductId(productId);
            Double rating = roundToTenth(productReviewStatisticsPort.getAverageMenuRatingByProductId(productId));
            product.updateReviewStats(rating, count != null ? count.intValue() : 0);
            productSavePort.save(product);
        });
    }

    private Double roundToTenth(Double rating) {
        return rating == null ? null : Math.round(rating * 10.0) / 10.0;
    }
}
