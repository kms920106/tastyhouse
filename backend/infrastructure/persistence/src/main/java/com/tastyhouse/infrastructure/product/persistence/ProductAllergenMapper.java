package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.product.model.ProductAllergen;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ProductAllergenMapper {
    private ProductAllergenMapper() {
    }

    static ProductAllergen toDomain(ProductAllergenJpaEntity entity) {
        return ProductAllergen.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getProductId(), ProductId::of),
            entity.getAllergenType(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ProductAllergenJpaEntity toEntity(ProductAllergen domain) {
        return ProductAllergenJpaEntity.create(
            IdMapping.raw(domain.getProductId(), ProductId::value),
            domain.getAllergenType()
        );
    }
}
