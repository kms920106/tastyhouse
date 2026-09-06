package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ProductOptionGroupMapper {
    private ProductOptionGroupMapper() {
    }

    static ProductOptionGroup toDomain(ProductOptionGroupJpaEntity entity) {
        return ProductOptionGroup.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getProductId(), ProductId::of),
            entity.getName(),
            entity.getDescription(),
            entity.isRequired(),
            entity.isMultipleSelect(),
            entity.getMinSelect(),
            entity.getMaxSelect(),
            entity.getSort(),
            entity.isVisible(),
            entity.getGroupType()
        );
    }

    static ProductOptionGroupJpaEntity toEntity(ProductOptionGroup domain) {
        return ProductOptionGroupJpaEntity.create(
            IdMapping.raw(domain.getProductId(), ProductId::value),
            domain.getName(),
            domain.getDescription(),
            domain.isRequired(),
            domain.isMultipleSelect(),
            domain.getMinSelect(),
            domain.getMaxSelect(),
            domain.getSort(),
            domain.isVisible(),
            domain.getGroupType()
        );
    }

    static void applyChanges(ProductOptionGroupJpaEntity entity, ProductOptionGroup domain) {
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
