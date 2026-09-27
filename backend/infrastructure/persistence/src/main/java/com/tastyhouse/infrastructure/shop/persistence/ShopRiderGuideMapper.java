package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.application.shop.port.out.write.ShopRiderGuideState;

final class ShopRiderGuideMapper {
    private ShopRiderGuideMapper() {
    }

    static ShopRiderGuideState toState(ShopRiderGuideJpaEntity entity) {
        return new ShopRiderGuideState(
            entity.getId(),
            entity.getShopId(),
            entity.getVisitGuide(),
            entity.getPickupRoadAddress(),
            entity.getPickupLotAddress(),
            entity.getPickupDetailAddress(),
            entity.getPickupLatitude(),
            entity.getPickupLongitude(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ShopRiderGuideJpaEntity toEntity(ShopRiderGuideState state) {
        return ShopRiderGuideJpaEntity.create(
            state.shopId(),
            state.visitGuide(),
            state.pickupRoadAddress(),
            state.pickupLotAddress(),
            state.pickupDetailAddress(),
            state.pickupLatitude(),
            state.pickupLongitude()
        );
    }

    static void applyChanges(ShopRiderGuideJpaEntity entity, ShopRiderGuideState state) {
        entity.applyChanges(
            state.visitGuide(),
            state.pickupRoadAddress(),
            state.pickupLotAddress(),
            state.pickupDetailAddress(),
            state.pickupLatitude(),
            state.pickupLongitude()
        );
    }
}
