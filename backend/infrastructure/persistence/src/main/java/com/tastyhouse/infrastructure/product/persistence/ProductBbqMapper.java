package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.application.product.port.out.write.ProductBbqState;

final class ProductBbqMapper {
    private ProductBbqMapper() {
    }

    static ProductBbqState toState(ProductBbqJpaEntity entity) {
        return new ProductBbqState(
            entity.getId(),
            entity.getProductId(),
            entity.getBbqMenuId(),
            entity.getBbqCategoryId(),
            entity.isOptionsSynced()
        );
    }

    static ProductBbqJpaEntity toEntity(ProductBbqState state) {
        return ProductBbqJpaEntity.create(
            state.productId(),
            state.bbqMenuId(),
            state.bbqCategoryId(),
            state.optionsSynced()
        );
    }

    static void applyChanges(ProductBbqJpaEntity entity, ProductBbqState state) {
        entity.applyChanges(state.optionsSynced());
    }
}
