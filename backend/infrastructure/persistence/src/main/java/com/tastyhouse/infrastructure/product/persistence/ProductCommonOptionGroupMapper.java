package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.application.product.port.out.write.ProductCommonOptionGroupState;

final class ProductCommonOptionGroupMapper {
    private ProductCommonOptionGroupMapper() {
    }

    static ProductCommonOptionGroupState toState(ProductCommonOptionGroupJpaEntity entity) {
        return new ProductCommonOptionGroupState(
            entity.getId(),
            entity.getProductId(),
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

    static ProductCommonOptionGroupJpaEntity toEntity(ProductCommonOptionGroupState state) {
        return ProductCommonOptionGroupJpaEntity.create(
            state.productId(),
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

    static void applyChanges(ProductCommonOptionGroupJpaEntity entity, ProductCommonOptionGroupState state) {
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
