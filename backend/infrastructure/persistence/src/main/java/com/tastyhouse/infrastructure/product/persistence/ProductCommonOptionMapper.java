package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.product.model.ProductCommonOption;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;

final class ProductCommonOptionMapper {
    private ProductCommonOptionMapper() {
    }

    static ProductCommonOption toDomain(ProductCommonOptionJpaEntity entity) {
        return ProductCommonOption.reconstitute(
            entity.getId(),
            entity.getOptionGroupId() == null ? null : ProductOptionGroupId.of(entity.getOptionGroupId()),
            entity.getName(),
            entity.getAdditionalPrice(),
            entity.getSort(),
            entity.isSoldOut(),
            entity.getSoldOutUntil(),
            entity.isVisible()
        );
    }

    static ProductCommonOptionJpaEntity toEntity(ProductCommonOption option) {
        return ProductCommonOptionJpaEntity.create(
            option.getOptionGroupId() == null ? null : option.getOptionGroupId().value(),
            option.getName(),
            option.getAdditionalPrice(),
            option.getSort(),
            option.isSoldOut(),
            option.getSoldOutUntil(),
            option.isVisible()
        );
    }

    static void applyChanges(ProductCommonOptionJpaEntity entity, ProductCommonOption option) {
        entity.applyChanges(
            option.getName(),
            option.getAdditionalPrice(),
            option.getSort(),
            option.isSoldOut(),
            option.getSoldOutUntil(),
            option.isVisible()
        );
    }
}
