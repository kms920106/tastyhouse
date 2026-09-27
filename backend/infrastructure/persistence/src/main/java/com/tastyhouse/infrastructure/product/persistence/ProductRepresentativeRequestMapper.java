package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.application.product.port.out.write.ProductRepresentativeRequestState;

final class ProductRepresentativeRequestMapper {
    private ProductRepresentativeRequestMapper() {
    }

    static ProductRepresentativeRequestState toState(ProductRepresentativeRequestJpaEntity entity) {
        return new ProductRepresentativeRequestState(
            entity.getId(),
            entity.getProductId(),
            entity.getShopId(),
            entity.getStatus(),
            entity.getRejectReason(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ProductRepresentativeRequestJpaEntity toEntity(ProductRepresentativeRequestState state) {
        return ProductRepresentativeRequestJpaEntity.create(
            state.productId(),
            state.shopId(),
            state.status(),
            state.rejectReason()
        );
    }

    static void applyChanges(
        ProductRepresentativeRequestJpaEntity entity,
        ProductRepresentativeRequestState state
    ) {
        entity.applyChanges(
            state.status(),
            state.rejectReason()
        );
    }
}
