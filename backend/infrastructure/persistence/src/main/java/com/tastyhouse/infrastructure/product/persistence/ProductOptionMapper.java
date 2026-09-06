package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ProductOptionMapper {
    private ProductOptionMapper() {
    }

    static ProductOption toDomain(ProductOptionJpaEntity entity) {
        return ProductOption.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getOptionGroupId(), ProductOptionGroupId::of),
            entity.getName(),
            entity.getAdditionalPrice(),
            entity.getSort(),
            entity.isSoldOut(),
            entity.getSoldOutUntil(),
            entity.isVisible(),
            entity.getCupCount(),
            entity.getPersonalCupDiscountAmount()
        );
    }

    static ProductOptionJpaEntity toEntity(ProductOption domain) {
        return ProductOptionJpaEntity.create(
            IdMapping.raw(domain.getOptionGroupId(), ProductOptionGroupId::value),
            domain.getName(),
            domain.getAdditionalPrice(),
            domain.getSort(),
            domain.isSoldOut(),
            domain.getSoldOutUntil(),
            domain.isVisible(),
            domain.getCupCount(),
            domain.getPersonalCupDiscountAmount()
        );
    }

    static void applyChanges(ProductOptionJpaEntity entity, ProductOption domain) {
        entity.applyChanges(
            domain.getName(),
            domain.getAdditionalPrice(),
            domain.getSort(),
            domain.isSoldOut(),
            domain.getSoldOutUntil(),
            domain.isVisible(),
            domain.getCupCount(),
            domain.getPersonalCupDiscountAmount()
        );
    }
}
