package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.product.model.AllergenType;
import com.tastyhouse.domain.product.model.ProductAllergen;
import com.tastyhouse.domain.product.vo.ProductId;

final class ProductAllergenMapper {
    private ProductAllergenMapper() {
    }

    static ProductAllergen toDomain(ProductAllergenJpaEntity entity) {
        return ProductAllergen.reconstitute(
            entity.getId(),
            entity.getProductId() == null ? null : ProductId.of(entity.getProductId()),
            entity.getAllergenType() == null ? null : AllergenType.valueOf(entity.getAllergenType()),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ProductAllergenJpaEntity toEntity(ProductAllergen productAllergen) {
        return ProductAllergenJpaEntity.create(
            productAllergen.getProductId() == null ? null : productAllergen.getProductId().value(),
            productAllergen.getAllergenType() == null ? null : productAllergen.getAllergenType().name()
        );
    }
}
