package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.product.port.out.ProductReviewStatisticsView;

public interface ProductReviewStatisticsQueryUseCase {

    ProductReviewStatisticsView getProductReviewStatistics(Long productId);
}
