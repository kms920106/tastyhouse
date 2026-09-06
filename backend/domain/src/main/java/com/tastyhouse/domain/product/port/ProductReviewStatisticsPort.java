package com.tastyhouse.domain.product.port;

public interface ProductReviewStatisticsPort {
    Long countVisibleMenuReviewsByProductId(Long productId);

    Double getAverageMenuRatingByProductId(Long productId);
}
