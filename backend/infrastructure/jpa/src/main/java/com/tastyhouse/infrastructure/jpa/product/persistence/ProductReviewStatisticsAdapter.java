package com.tastyhouse.infrastructure.jpa.product.persistence;

import org.springframework.stereotype.Component;

import com.tastyhouse.application.product.port.out.ProductReviewStatisticsPort;
import com.tastyhouse.infrastructure.jpa.menureview.query.MenuReviewStatisticsQueryAdapter;

@Component
class ProductReviewStatisticsAdapter implements ProductReviewStatisticsPort {

    private final MenuReviewStatisticsQueryAdapter menuReviewStatisticsQueryAdapter;

    public ProductReviewStatisticsAdapter(MenuReviewStatisticsQueryAdapter menuReviewStatisticsQueryAdapter) {
        this.menuReviewStatisticsQueryAdapter = menuReviewStatisticsQueryAdapter;
    }

    @Override
    public Long countVisibleMenuReviewsByProductId(Long productId) {
        return menuReviewStatisticsQueryAdapter.countVisibleByProductId(productId);
    }

    @Override
    public Double getAverageMenuRatingByProductId(Long productId) {
        return menuReviewStatisticsQueryAdapter.getAverageRatingByProductId(productId);
    }
}
