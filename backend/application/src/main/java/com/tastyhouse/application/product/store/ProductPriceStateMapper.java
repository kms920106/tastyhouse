package com.tastyhouse.application.product.store;

import com.tastyhouse.application.product.port.out.write.ProductPriceState;
import com.tastyhouse.domain.product.model.ProductPrice;
import com.tastyhouse.domain.product.vo.ProductId;

final class ProductPriceStateMapper {
    private ProductPriceStateMapper() {
    }

    static ProductPrice toDomain(ProductPriceState state) {
        return ProductPrice.reconstitute(
            state.id(),
            state.productId() == null ? null : ProductId.of(state.productId()),
            state.priceName(),
            state.deliveryPrice(),
            state.storePrice(),
            state.pickupPrice(),
            state.sort(),
            state.pickupPriceSetAt(),
            state.createdAt(),
            state.updatedAt()
        );
    }

    static ProductPriceState toState(ProductPrice price) {
        return new ProductPriceState(
            price.getId(),
            price.getProductId() == null ? null : price.getProductId().value(),
            price.getPriceName(),
            price.getDeliveryPrice(),
            price.getStorePrice(),
            price.getPickupPrice(),
            price.getSort(),
            price.getPickupPriceSetAt(),
            price.getCreatedAt(),
            price.getUpdatedAt()
        );
    }
}
