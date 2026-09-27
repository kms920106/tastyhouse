package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.application.product.port.out.write.ProductAllergenState;

final class ProductAllergenMapper {
    private ProductAllergenMapper() {
    }

    static ProductAllergenState toState(ProductAllergenJpaEntity entity) {
        return new ProductAllergenState(
            entity.getId(),
            entity.getProductId(),
            entity.getAllergenType(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ProductAllergenJpaEntity toEntity(ProductAllergenState state) {
        return ProductAllergenJpaEntity.create(
            state.productId(),
            state.allergenType()
        );
    }
}
