package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.application.shop.port.out.write.ShopSuspensionState;

final class ShopSuspensionMapper {
    private ShopSuspensionMapper() {
    }

    static ShopSuspensionState toState(ShopSuspensionJpaEntity entity) {
        return new ShopSuspensionState(
            entity.getId(),
            entity.getShopId(),
            entity.getReason(),
            entity.getOrderMethod(),
            entity.getStartAt(),
            entity.getEndAt(),
            entity.getReleasedAt(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ShopSuspensionJpaEntity toEntity(ShopSuspensionState state) {
        return ShopSuspensionJpaEntity.create(
            state.shopId(),
            state.reason(),
            state.orderMethod(),
            state.startAt(),
            state.endAt(),
            state.releasedAt()
        );
    }

    static void applyChanges(ShopSuspensionJpaEntity entity, ShopSuspensionState state) {
        entity.applyChanges(state.releasedAt());
    }
}
