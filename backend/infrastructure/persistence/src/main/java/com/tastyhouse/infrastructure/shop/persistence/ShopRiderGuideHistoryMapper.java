package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.application.shop.port.out.write.ShopRiderGuideHistoryState;

final class ShopRiderGuideHistoryMapper {
    private ShopRiderGuideHistoryMapper() {
    }

    static ShopRiderGuideHistoryState toState(ShopRiderGuideHistoryJpaEntity entity) {
        return new ShopRiderGuideHistoryState(
            entity.getId(),
            entity.getShopId(),
            entity.getActorType(),
            entity.getActorId(),
            entity.getActionType(),
            entity.getPreviousVisitGuide(),
            entity.getNewVisitGuide(),
            entity.getReason(),
            entity.getCreatedAt()
        );
    }

    static ShopRiderGuideHistoryJpaEntity toEntity(ShopRiderGuideHistoryState state) {
        return ShopRiderGuideHistoryJpaEntity.create(
            state.shopId(),
            state.actorType(),
            state.actorId(),
            state.actionType(),
            state.previousVisitGuide(),
            state.newVisitGuide(),
            state.reason()
        );
    }
}
