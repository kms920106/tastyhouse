package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.application.shop.port.out.write.ShopChangeHistoryState;

final class ShopChangeHistoryMapper {
    private ShopChangeHistoryMapper() {
    }

    static ShopChangeHistoryState toState(ShopChangeHistoryJpaEntity entity) {
        return new ShopChangeHistoryState(
            entity.getId(),
            entity.getShopId(),
            entity.getCategory(),
            entity.getChangeType(),
            entity.getActionType(),
            entity.getActorType(),
            entity.getActorId(),
            entity.getPreviousValue(),
            entity.getNewValue(),
            entity.getCreatedAt()
        );
    }

    static ShopChangeHistoryJpaEntity toEntity(ShopChangeHistoryState state) {
        return ShopChangeHistoryJpaEntity.create(
            state.shopId(),
            state.category(),
            state.changeType(),
            state.actionType(),
            state.actorType(),
            state.actorId(),
            state.previousValue(),
            state.newValue()
        );
    }
}
