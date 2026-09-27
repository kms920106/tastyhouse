package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.application.product.port.out.write.StorePriceVerificationItemState;

final class StorePriceVerificationItemMapper {
    private StorePriceVerificationItemMapper() {
    }

    static StorePriceVerificationItemState toState(StorePriceVerificationItemJpaEntity entity) {
        return new StorePriceVerificationItemState(
            entity.getId(),
            entity.getVerificationId(),
            entity.getProductId(),
            entity.getProductPriceId(),
            entity.getStorePrice(),
            entity.isApplyPickupSamePrice(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static StorePriceVerificationItemJpaEntity toEntity(StorePriceVerificationItemState state) {
        return StorePriceVerificationItemJpaEntity.create(
            state.verificationId(),
            state.productId(),
            state.productPriceId(),
            state.storePrice(),
            state.applyPickupSamePrice()
        );
    }
}
