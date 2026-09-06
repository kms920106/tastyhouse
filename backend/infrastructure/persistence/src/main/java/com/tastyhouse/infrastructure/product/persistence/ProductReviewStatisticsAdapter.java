package com.tastyhouse.infrastructure.product.persistence;

import org.springframework.stereotype.Component;

import com.tastyhouse.domain.product.port.ProductReviewStatisticsPort;
import com.tastyhouse.infrastructure.menureview.query.MenuReviewStatisticsQueryDao;

@Component
public class ProductReviewStatisticsAdapter implements ProductReviewStatisticsPort {
    private final MenuReviewStatisticsQueryDao menuReviewStatisticsQueryDao;

    public ProductReviewStatisticsAdapter(MenuReviewStatisticsQueryDao menuReviewStatisticsQueryDao) {
        this.menuReviewStatisticsQueryDao = menuReviewStatisticsQueryDao;
    }

    @Override
    public Long countVisibleMenuReviewsByProductId(Long productId) {
        return menuReviewStatisticsQueryDao.countVisibleByProductId(productId);
    }

    @Override
    public Double getAverageMenuRatingByProductId(Long productId) {
        return menuReviewStatisticsQueryDao.getAverageRatingByProductId(productId);
    }
}
