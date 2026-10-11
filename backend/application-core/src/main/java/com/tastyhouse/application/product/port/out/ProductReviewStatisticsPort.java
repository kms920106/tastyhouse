package com.tastyhouse.application.product.port.out;

public interface ProductReviewStatisticsPort {

    Long countVisibleMenuReviewsByProductId(Long productId);

    Double getAverageMenuRatingByProductId(Long productId);
}
