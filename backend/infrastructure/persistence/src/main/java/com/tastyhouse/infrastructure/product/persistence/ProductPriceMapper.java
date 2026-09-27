package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.application.product.port.out.write.ProductPriceState;

final class ProductPriceMapper {
    private ProductPriceMapper() {
    }

    static ProductPriceState toState(ProductPriceJpaEntity entity) {
        return new ProductPriceState(
            entity.getId(),
            entity.getProductId(),
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

    static ProductPriceJpaEntity toEntity(ProductPriceState state) {
        return ProductPriceJpaEntity.create(
            state.productId(),
            state.priceName(),
            state.deliveryPrice(),
            state.storePrice(),
            state.pickupPrice(),
            state.sort(),
            state.pickupPriceSetAt()
        );
    }

    static void applyChanges(ProductPriceJpaEntity entity, ProductPriceState state) {
        entity.applyChanges(
            state.priceName(),
            state.deliveryPrice(),
            state.storePrice(),
            state.pickupPrice(),
            state.sort(),
            state.pickupPriceSetAt()
        );
    }
}
