package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.product.model.ProductRepresentativeRequest;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ProductRepresentativeRequestMapper {
    private ProductRepresentativeRequestMapper() {
    }

    static ProductRepresentativeRequest toDomain(ProductRepresentativeRequestJpaEntity entity) {
        return ProductRepresentativeRequest.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getProductId(), ProductId::of),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            entity.getStatus(),
            entity.getRejectReason(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ProductRepresentativeRequestJpaEntity toEntity(ProductRepresentativeRequest domain) {
        return ProductRepresentativeRequestJpaEntity.create(
            IdMapping.raw(domain.getProductId(), ProductId::value),
            IdMapping.raw(domain.getShopId(), ShopId::value),
            domain.getStatus(),
            domain.getRejectReason()
        );
    }

    static void applyChanges(
        ProductRepresentativeRequestJpaEntity entity,
        ProductRepresentativeRequest domain
    ) {
        entity.applyChanges(
            domain.getStatus(),
            domain.getRejectReason()
        );
    }
}
