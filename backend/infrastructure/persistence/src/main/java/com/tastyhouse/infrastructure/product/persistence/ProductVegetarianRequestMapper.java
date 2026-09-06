package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.product.model.ProductVegetarianRequest;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ProductVegetarianRequestMapper {
    private ProductVegetarianRequestMapper() {
    }

    static ProductVegetarianRequest toDomain(ProductVegetarianRequestJpaEntity entity) {
        return ProductVegetarianRequest.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getProductId(), ProductId::of),
            entity.getVegetarianType(),
            entity.getIngredients(),
            entity.getDescription(),
            entity.getStatus(),
            entity.getRejectReason(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ProductVegetarianRequestJpaEntity toEntity(ProductVegetarianRequest domain) {
        return ProductVegetarianRequestJpaEntity.create(
            IdMapping.raw(domain.getProductId(), ProductId::value),
            domain.getVegetarianType(),
            domain.getIngredients(),
            domain.getDescription(),
            domain.getStatus(),
            domain.getRejectReason()
        );
    }

    static void applyChanges(ProductVegetarianRequestJpaEntity entity, ProductVegetarianRequest domain) {
        entity.applyChanges(
            domain.getStatus(),
            domain.getRejectReason()
        );
    }
}
