package com.tastyhouse.infrastructure.persistence.product.persistence;

import com.tastyhouse.domain.product.model.ProductPrice;
import com.tastyhouse.domain.product.vo.ProductId;

final class ProductPriceMapper {

    private ProductPriceMapper() {
    }

    static ProductPrice toDomain(ProductPriceJpaEntity entity) {
        return ProductPrice.reconstitute(
            entity.getId(),
            entity.getProductId() == null ? null : ProductId.of(entity.getProductId()),
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

    static ProductPriceJpaEntity toEntity(ProductPrice price) {
        return ProductPriceJpaEntity.create(
            price.getProductId() == null ? null : price.getProductId().value(),
            price.getPriceName(),
            price.getDeliveryPrice(),
            price.getStorePrice(),
            price.getPickupPrice(),
            price.getSort(),
            price.getPickupPriceSetAt()
        );
    }

    static void applyChanges(ProductPriceJpaEntity entity, ProductPrice price) {
        entity.applyChanges(
            price.getPriceName(),
            price.getDeliveryPrice(),
            price.getStorePrice(),
            price.getPickupPrice(),
            price.getSort(),
            price.getPickupPriceSetAt()
        );
    }
}
