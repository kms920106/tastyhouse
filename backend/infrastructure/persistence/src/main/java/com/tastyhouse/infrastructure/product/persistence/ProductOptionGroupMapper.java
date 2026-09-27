package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.application.product.port.out.write.ProductOptionGroupState;

final class ProductOptionGroupMapper {
    private ProductOptionGroupMapper() {
    }

    static ProductOptionGroupState toState(ProductOptionGroupJpaEntity entity) {
        return new ProductOptionGroupState(
            entity.getId(),
            entity.getProductId(),
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

    static ProductOptionGroupJpaEntity toEntity(ProductOptionGroupState state) {
        return ProductOptionGroupJpaEntity.create(
            state.productId(),
            state.name(),
            state.description(),
            state.required(),
            state.multipleSelect(),
            state.minSelect(),
            state.maxSelect(),
            state.sort(),
            state.visible(),
            state.groupType()
        );
    }

    static void applyChanges(ProductOptionGroupJpaEntity entity, ProductOptionGroupState state) {
        entity.applyChanges(
            state.name(),
            state.description(),
            state.required(),
            state.multipleSelect(),
            state.minSelect(),
            state.maxSelect(),
            state.sort(),
            state.visible()
        );
    }
}
