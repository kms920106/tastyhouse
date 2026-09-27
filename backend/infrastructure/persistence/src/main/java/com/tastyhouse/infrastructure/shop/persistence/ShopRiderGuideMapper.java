package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shop.model.ShopRiderGuide;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ShopRiderGuideMapper {
    private ShopRiderGuideMapper() {
    }

    static ShopRiderGuide toDomain(ShopRiderGuideJpaEntity entity) {
        return ShopRiderGuide.reconstitute(
            entity.getId(),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
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

    static ShopRiderGuideJpaEntity toEntity(ShopRiderGuide shopRiderGuide) {
        return ShopRiderGuideJpaEntity.create(
            shopRiderGuide.getShopId() == null ? null : shopRiderGuide.getShopId().value(),
            shopRiderGuide.getVisitGuide(),
            shopRiderGuide.getPickupRoadAddress(),
            shopRiderGuide.getPickupLotAddress(),
            shopRiderGuide.getPickupDetailAddress(),
            shopRiderGuide.getPickupLatitude(),
            shopRiderGuide.getPickupLongitude()
        );
    }

    static void applyChanges(ShopRiderGuideJpaEntity entity, ShopRiderGuide shopRiderGuide) {
        entity.applyChanges(
            shopRiderGuide.getVisitGuide(),
            shopRiderGuide.getPickupRoadAddress(),
            shopRiderGuide.getPickupLotAddress(),
            shopRiderGuide.getPickupDetailAddress(),
            shopRiderGuide.getPickupLatitude(),
            shopRiderGuide.getPickupLongitude()
        );
    }
}
