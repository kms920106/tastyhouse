package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.application.product.port.out.write.ProductVegetarianRequestState;

final class ProductVegetarianRequestMapper {
    private ProductVegetarianRequestMapper() {
    }

    static ProductVegetarianRequestState toState(ProductVegetarianRequestJpaEntity entity) {
        return new ProductVegetarianRequestState(
            entity.getId(),
            entity.getProductId(),
            entity.getVegetarianType(),
            entity.getIngredients(),
            entity.getDescription(),
            entity.getStatus(),
            entity.getRejectReason(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ProductVegetarianRequestJpaEntity toEntity(ProductVegetarianRequestState state) {
        return ProductVegetarianRequestJpaEntity.create(
            state.productId(),
            state.vegetarianType(),
            state.ingredients(),
            state.description(),
            state.status(),
            state.rejectReason()
        );
    }

    static void applyChanges(ProductVegetarianRequestJpaEntity entity, ProductVegetarianRequestState state) {
        entity.applyChanges(
            state.status(),
            state.rejectReason()
        );
    }
}
