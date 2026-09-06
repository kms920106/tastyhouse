package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.product.model.ProductCommonOptionGroup;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ProductCommonOptionGroupMapper {
    private ProductCommonOptionGroupMapper() {
    }

    static ProductCommonOptionGroup toDomain(ProductCommonOptionGroupJpaEntity entity) {
        return ProductCommonOptionGroup.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getProductId(), ProductId::of),
            entity.getName(),
            entity.getDescription(),
            entity.isRequired(),
            entity.isMultipleSelect(),
            entity.getMinSelect(),
            entity.getMaxSelect(),
            entity.getSort(),
            entity.isVisible()
        );
    }

    static ProductCommonOptionGroupJpaEntity toEntity(ProductCommonOptionGroup domain) {
        return ProductCommonOptionGroupJpaEntity.create(
            IdMapping.raw(domain.getProductId(), ProductId::value),
            domain.getName(),
            domain.getDescription(),
            domain.isRequired(),
            domain.isMultipleSelect(),
            domain.getMinSelect(),
            domain.getMaxSelect(),
            domain.getSort(),
            domain.isVisible()
        );
    }

    static void applyChanges(ProductCommonOptionGroupJpaEntity entity, ProductCommonOptionGroup domain) {
        entity.applyChanges(
            domain.getName(),
            domain.getDescription(),
            domain.isRequired(),
            domain.isMultipleSelect(),
            domain.getMinSelect(),
            domain.getMaxSelect(),
            domain.getSort(),
            domain.isVisible()
        );
    }
}
