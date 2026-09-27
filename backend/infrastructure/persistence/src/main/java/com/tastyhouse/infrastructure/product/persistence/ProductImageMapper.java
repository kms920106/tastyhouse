package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.application.product.port.out.write.ProductImageState;

final class ProductImageMapper {
    private ProductImageMapper() {
    }

    static ProductImageState toState(ProductImageJpaEntity entity) {
        return new ProductImageState(
            entity.getId(),
            entity.getProductId(),
            entity.getImageFileId(),
            entity.getSort(),
            entity.isVisible()
        );
    }

    static ProductImageJpaEntity toEntity(ProductImageState state) {
        return ProductImageJpaEntity.create(
            state.productId(),
            state.imageFileId(),
            state.sort(),
            state.visible()
        );
    }

    static void applyChanges(ProductImageJpaEntity entity, ProductImageState state) {
        entity.applyChanges(state.sort(), state.visible());
    }
}
