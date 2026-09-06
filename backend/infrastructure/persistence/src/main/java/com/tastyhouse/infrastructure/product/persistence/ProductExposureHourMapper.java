package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.product.model.ProductExposureHour;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ProductExposureHourMapper {
    private ProductExposureHourMapper() {
    }

    static ProductExposureHour toDomain(ProductExposureHourJpaEntity entity) {
        return ProductExposureHour.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getProductId(), ProductId::of),
            entity.getDayType(),
            entity.getStartTime(),
            entity.getEndTime()
        );
    }

    static ProductExposureHourJpaEntity toEntity(ProductExposureHour domain) {
        return ProductExposureHourJpaEntity.create(
            IdMapping.raw(domain.getProductId(), ProductId::value),
            domain.getDayType(),
            domain.getStartTime(),
            domain.getEndTime()
        );
    }
}
