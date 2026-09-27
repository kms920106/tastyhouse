package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.application.product.port.out.write.ProductImageChangeRequestState;

final class ProductImageChangeRequestMapper {
    private ProductImageChangeRequestMapper() {
    }

    static ProductImageChangeRequestState toState(ProductImageChangeRequestJpaEntity entity) {
        return new ProductImageChangeRequestState(
            entity.getId(),
            entity.getProductId(),
            entity.getImageFileId(),
            entity.getStatus(),
            entity.getRejectReason(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ProductImageChangeRequestJpaEntity toEntity(ProductImageChangeRequestState state) {
        return ProductImageChangeRequestJpaEntity.create(
            state.productId(),
            state.imageFileId(),
            state.status(),
            state.rejectReason()
        );
    }

    static void applyChanges(ProductImageChangeRequestJpaEntity entity, ProductImageChangeRequestState state) {
        entity.applyChanges(
            state.status(),
            state.rejectReason()
        );
    }
}
