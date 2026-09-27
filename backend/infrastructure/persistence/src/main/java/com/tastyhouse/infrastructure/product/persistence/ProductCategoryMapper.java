package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.application.product.port.out.write.ProductCategoryState;

final class ProductCategoryMapper {
    private ProductCategoryMapper() {
    }

    static ProductCategoryState toState(ProductCategoryJpaEntity entity) {
        return new ProductCategoryState(
            entity.getId(),
            entity.getShopId(),
            entity.getName(),
            entity.getDescription(),
            entity.getSort(),
            entity.isVisible()
        );
    }

    static ProductCategoryJpaEntity toEntity(ProductCategoryState state) {
        return ProductCategoryJpaEntity.create(
            state.shopId(),
            state.name(),
            state.description(),
            state.sort(),
            state.visible()
        );
    }

    static void applyChanges(ProductCategoryJpaEntity entity, ProductCategoryState state) {
        entity.applyChanges(
            state.name(),
            state.description(),
            state.sort(),
            state.visible()
        );
    }
}
