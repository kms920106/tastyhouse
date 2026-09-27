package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.application.shop.port.out.write.ShopDeliveryAreaAdjustmentRequestState;

final class ShopDeliveryAreaAdjustmentRequestMapper {
    private ShopDeliveryAreaAdjustmentRequestMapper() {
    }

    static ShopDeliveryAreaAdjustmentRequestState toState(ShopDeliveryAreaAdjustmentRequestJpaEntity entity) {
        return new ShopDeliveryAreaAdjustmentRequestState(
            entity.getId(),
            entity.getShopId(),
            entity.getCounterpartShopName(),
            entity.getCounterpartBusinessNumber(),
            entity.getFranchiseName(),
            entity.getReason(),
            entity.getConsentFileId(),
            entity.getStatus(),
            entity.getRejectReason(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ShopDeliveryAreaAdjustmentRequestJpaEntity toEntity(ShopDeliveryAreaAdjustmentRequestState state) {
        return ShopDeliveryAreaAdjustmentRequestJpaEntity.create(
            state.shopId(),
            state.counterpartShopName(),
            state.counterpartBusinessNumber(),
            state.franchiseName(),
            state.reason(),
            state.consentFileId(),
            state.status(),
            state.rejectReason()
        );
    }

    static void applyChanges(ShopDeliveryAreaAdjustmentRequestJpaEntity entity, ShopDeliveryAreaAdjustmentRequestState state) {
        entity.applyChanges(
            state.status(),
            state.rejectReason()
        );
    }
}
