package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.application.shop.port.out.write.ShopHygieneBadgeState;

final class ShopHygieneBadgeMapper {
    private ShopHygieneBadgeMapper() {
    }

    static ShopHygieneBadgeState toState(ShopHygieneBadgeJpaEntity entity) {
        return new ShopHygieneBadgeState(
            entity.getId(),
            entity.getShopId(),
            entity.getBadgeType(),
            entity.getCertifiedDate(),
            entity.getLastInspectionMonth(),
            entity.getCreatedAt()
        );
    }

    static ShopHygieneBadgeJpaEntity toEntity(ShopHygieneBadgeState state) {
        return ShopHygieneBadgeJpaEntity.create(
            state.shopId(),
            state.badgeType(),
            state.certifiedDate(),
            state.lastInspectionMonth()
        );
    }
}
