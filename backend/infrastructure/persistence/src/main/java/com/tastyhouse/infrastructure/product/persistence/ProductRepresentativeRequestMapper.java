package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.product.model.ProductRepresentativeRequest;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ProductRepresentativeRequestMapper {
    private ProductRepresentativeRequestMapper() {
    }

    static ProductRepresentativeRequest toDomain(ProductRepresentativeRequestJpaEntity entity) {
        return ProductRepresentativeRequest.reconstitute(
            entity.getId(),
            entity.getProductId() == null ? null : ProductId.of(entity.getProductId()),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
            entity.getStatus() == null ? null : ApprovalStatus.valueOf(entity.getStatus()),
            entity.getRejectReason(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ProductRepresentativeRequestJpaEntity toEntity(ProductRepresentativeRequest request) {
        return ProductRepresentativeRequestJpaEntity.create(
            request.getProductId() == null ? null : request.getProductId().value(),
            request.getShopId() == null ? null : request.getShopId().value(),
            request.getStatus() == null ? null : request.getStatus().name(),
            request.getRejectReason()
        );
    }

    static void applyChanges(
        ProductRepresentativeRequestJpaEntity entity,
        ProductRepresentativeRequest request
    ) {
        entity.applyChanges(
            request.getStatus() == null ? null : request.getStatus().name(),
            request.getRejectReason()
        );
    }
}
