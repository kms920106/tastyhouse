package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.application.product.port.out.write.StorePriceVerificationState;

final class StorePriceVerificationMapper {
    private StorePriceVerificationMapper() {
    }

    static StorePriceVerificationState toState(StorePriceVerificationJpaEntity entity) {
        return new StorePriceVerificationState(
            entity.getId(),
            entity.getShopId(),
            entity.getPriceListFileId(),
            entity.getStatus(),
            entity.getRejectReason(),
            entity.getRequestedByCeoId(),
            entity.getProcessedAt(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static StorePriceVerificationJpaEntity toEntity(StorePriceVerificationState state) {
        return StorePriceVerificationJpaEntity.create(
            state.shopId(),
            state.priceListFileId(),
            state.status(),
            state.rejectReason(),
            state.requestedByCeoId(),
            state.processedAt()
        );
    }

    static void applyChanges(StorePriceVerificationJpaEntity entity, StorePriceVerificationState state) {
        entity.applyChanges(
            state.status(),
            state.rejectReason(),
            state.processedAt()
        );
    }
}
