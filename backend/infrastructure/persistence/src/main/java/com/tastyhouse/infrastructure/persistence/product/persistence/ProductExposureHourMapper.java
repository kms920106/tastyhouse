package com.tastyhouse.infrastructure.persistence.product.persistence;

import com.tastyhouse.domain.product.model.ProductExposureHour;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shared.model.DayType;

final class ProductExposureHourMapper {

    private ProductExposureHourMapper() {
    }

    static ProductExposureHour toDomain(ProductExposureHourJpaEntity entity) {
        return ProductExposureHour.reconstitute(
            entity.getId(),
            entity.getProductId() == null ? null : ProductId.of(entity.getProductId()),
            entity.getDayType() == null ? null : DayType.valueOf(entity.getDayType()),
            entity.getStartTime(),
            entity.getEndTime()
        );
    }

    static ProductExposureHourJpaEntity toEntity(ProductExposureHour hour) {
        return ProductExposureHourJpaEntity.create(
            hour.getProductId() == null ? null : hour.getProductId().value(),
            hour.getDayType() == null ? null : hour.getDayType().name(),
            hour.getStartTime(),
            hour.getEndTime()
        );
    }
}
