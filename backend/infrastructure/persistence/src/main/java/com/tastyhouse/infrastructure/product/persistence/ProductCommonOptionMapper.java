package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.product.model.ProductCommonOption;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ProductCommonOptionMapper {
    private ProductCommonOptionMapper() {
    }

    static ProductCommonOption toDomain(ProductCommonOptionJpaEntity entity) {
        return ProductCommonOption.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getOptionGroupId(), ProductOptionGroupId::of),
            entity.getName(),
            entity.getAdditionalPrice(),
            entity.getSort(),
            entity.isSoldOut(),
            entity.getSoldOutUntil(),
            entity.isVisible()
        );
    }

    static ProductCommonOptionJpaEntity toEntity(ProductCommonOption domain) {
        return ProductCommonOptionJpaEntity.create(
            IdMapping.raw(domain.getOptionGroupId(), ProductOptionGroupId::value),
            domain.getName(),
            domain.getAdditionalPrice(),
            domain.getSort(),
            domain.isSoldOut(),
            domain.getSoldOutUntil(),
            domain.isVisible()
        );
    }

    static void applyChanges(ProductCommonOptionJpaEntity entity, ProductCommonOption domain) {
        entity.applyChanges(
            domain.getName(),
            domain.getAdditionalPrice(),
            domain.getSort(),
            domain.isSoldOut(),
            domain.getSoldOutUntil(),
            domain.isVisible()
        );
    }
}
