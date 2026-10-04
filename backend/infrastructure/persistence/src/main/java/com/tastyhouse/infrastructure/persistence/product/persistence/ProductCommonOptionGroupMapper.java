package com.tastyhouse.infrastructure.persistence.product.persistence;

import com.tastyhouse.domain.product.model.ProductCommonOptionGroup;
import com.tastyhouse.domain.product.vo.ProductId;

final class ProductCommonOptionGroupMapper {

    private ProductCommonOptionGroupMapper() {
    }

    static ProductCommonOptionGroup toDomain(ProductCommonOptionGroupJpaEntity entity) {
        return ProductCommonOptionGroup.reconstitute(
            entity.getId(),
            entity.getProductId() == null ? null : ProductId.of(entity.getProductId()),
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

    static ProductCommonOptionGroupJpaEntity toEntity(ProductCommonOptionGroup group) {
        return ProductCommonOptionGroupJpaEntity.create(
            group.getProductId() == null ? null : group.getProductId().value(),
            group.getName(),
            group.getDescription(),
            group.isRequired(),
            group.isMultipleSelect(),
            group.getMinSelect(),
            group.getMaxSelect(),
            group.getSort(),
            group.isVisible()
        );
    }

    static void applyChanges(ProductCommonOptionGroupJpaEntity entity, ProductCommonOptionGroup group) {
        entity.applyChanges(
            group.getName(),
            group.getDescription(),
            group.isRequired(),
            group.isMultipleSelect(),
            group.getMinSelect(),
            group.getMaxSelect(),
            group.getSort(),
            group.isVisible()
        );
    }
}
