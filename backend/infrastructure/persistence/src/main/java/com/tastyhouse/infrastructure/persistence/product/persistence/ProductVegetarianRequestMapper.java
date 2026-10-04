package com.tastyhouse.infrastructure.persistence.product.persistence;

import com.tastyhouse.domain.product.model.ProductVegetarianRequest;
import com.tastyhouse.domain.product.model.VegetarianType;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;

final class ProductVegetarianRequestMapper {

    private ProductVegetarianRequestMapper() {
    }

    static ProductVegetarianRequest toDomain(ProductVegetarianRequestJpaEntity entity) {
        return ProductVegetarianRequest.reconstitute(
            entity.getId(),
            entity.getProductId() == null ? null : ProductId.of(entity.getProductId()),
            entity.getVegetarianType() == null ? null : VegetarianType.valueOf(entity.getVegetarianType()),
            entity.getIngredients(),
            entity.getDescription(),
            entity.getStatus() == null ? null : ApprovalStatus.valueOf(entity.getStatus()),
            entity.getRejectReason(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ProductVegetarianRequestJpaEntity toEntity(ProductVegetarianRequest request) {
        return ProductVegetarianRequestJpaEntity.create(
            request.getProductId() == null ? null : request.getProductId().value(),
            request.getVegetarianType() == null ? null : request.getVegetarianType().name(),
            request.getIngredients(),
            request.getDescription(),
            request.getStatus() == null ? null : request.getStatus().name(),
            request.getRejectReason()
        );
    }

    static void applyChanges(ProductVegetarianRequestJpaEntity entity, ProductVegetarianRequest request) {
        entity.applyChanges(
            request.getStatus() == null ? null : request.getStatus().name(),
            request.getRejectReason()
        );
    }
}
