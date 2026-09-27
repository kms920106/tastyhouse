package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.application.product.port.out.write.ProductExposureHourState;

final class ProductExposureHourMapper {
    private ProductExposureHourMapper() {
    }

    static ProductExposureHourState toState(ProductExposureHourJpaEntity entity) {
        return new ProductExposureHourState(
            entity.getId(),
            entity.getProductId(),
            entity.getDayType(),
            entity.getStartTime(),
            entity.getEndTime()
        );
    }

    static ProductExposureHourJpaEntity toEntity(ProductExposureHourState state) {
        return ProductExposureHourJpaEntity.create(
            state.productId(),
            state.dayType(),
            state.startTime(),
            state.endTime()
        );
    }
}
