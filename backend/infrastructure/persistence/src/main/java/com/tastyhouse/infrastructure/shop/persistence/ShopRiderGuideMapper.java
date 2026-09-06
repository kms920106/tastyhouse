package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shop.model.ShopRiderGuide;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ShopRiderGuideMapper {
    private ShopRiderGuideMapper() {
    }

    static ShopRiderGuide toDomain(ShopRiderGuideJpaEntity entity) {
        return ShopRiderGuide.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
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

    static ShopRiderGuideJpaEntity toEntity(ShopRiderGuide domain) {
        return ShopRiderGuideJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            domain.getVisitGuide(),
            domain.getPickupRoadAddress(),
            domain.getPickupLotAddress(),
            domain.getPickupDetailAddress(),
            domain.getPickupLatitude(),
            domain.getPickupLongitude()
        );
    }

    static void applyChanges(ShopRiderGuideJpaEntity entity, ShopRiderGuide domain) {
        entity.applyChanges(
            domain.getVisitGuide(),
            domain.getPickupRoadAddress(),
            domain.getPickupLotAddress(),
            domain.getPickupDetailAddress(),
            domain.getPickupLatitude(),
            domain.getPickupLongitude()
        );
    }
}
