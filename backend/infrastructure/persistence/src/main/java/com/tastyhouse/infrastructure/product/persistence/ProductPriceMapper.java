package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.product.model.ProductPrice;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ProductPriceMapper {
    private ProductPriceMapper() {
    }

    static ProductPrice toDomain(ProductPriceJpaEntity entity) {
        return ProductPrice.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getProductId(), ProductId::of),
            entity.getPriceName(),
            entity.getDeliveryPrice(),
            entity.getStorePrice(),
            entity.getPickupPrice(),
            entity.getSort(),
            entity.getPickupPriceSetAt(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ProductPriceJpaEntity toEntity(ProductPrice domain) {
        return ProductPriceJpaEntity.create(
            IdMapping.raw(domain.getProductId(), ProductId::value),
            domain.getPriceName(),
            domain.getDeliveryPrice(),
            domain.getStorePrice(),
            domain.getPickupPrice(),
            domain.getSort(),
            domain.getPickupPriceSetAt()
        );
    }

    static void applyChanges(ProductPriceJpaEntity entity, ProductPrice domain) {
        entity.applyChanges(
            domain.getPriceName(),
            domain.getDeliveryPrice(),
            domain.getStorePrice(),
            domain.getPickupPrice(),
            domain.getSort(),
            domain.getPickupPriceSetAt()
        );
    }
}
