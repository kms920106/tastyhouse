package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.model.ProductOptionGroupType;
import com.tastyhouse.domain.product.vo.ProductId;

final class ProductOptionGroupMapper {

    private ProductOptionGroupMapper() {
    }

    static ProductOptionGroup toDomain(ProductOptionGroupJpaEntity entity) {
        return ProductOptionGroup.reconstitute(
            entity.getId(),
            entity.getProductId() == null ? null : ProductId.of(entity.getProductId()),
            entity.getName(),
            entity.getDescription(),
            entity.isRequired(),
            entity.isMultipleSelect(),
            entity.getMinSelect(),
            entity.getMaxSelect(),
            entity.getSort(),
            entity.isVisible(),
            entity.getGroupType() == null ? null : ProductOptionGroupType.valueOf(entity.getGroupType())
        );
    }

    static ProductOptionGroupJpaEntity toEntity(ProductOptionGroup group) {
        return ProductOptionGroupJpaEntity.create(
            group.getProductId() == null ? null : group.getProductId().value(),
            group.getName(),
            group.getDescription(),
            group.isRequired(),
            group.isMultipleSelect(),
            group.getMinSelect(),
            group.getMaxSelect(),
            group.getSort(),
            group.isVisible(),
            group.getGroupType() == null ? null : group.getGroupType().name()
        );
    }

    static void applyChanges(ProductOptionGroupJpaEntity entity, ProductOptionGroup group) {
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
