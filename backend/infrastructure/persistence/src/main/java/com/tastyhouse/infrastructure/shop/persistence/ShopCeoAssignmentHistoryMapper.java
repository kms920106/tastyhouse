package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.application.shop.port.out.write.ShopCeoAssignmentHistoryState;

final class ShopCeoAssignmentHistoryMapper {
    private ShopCeoAssignmentHistoryMapper() {
    }

    static ShopCeoAssignmentHistoryState toState(ShopCeoAssignmentHistoryJpaEntity entity) {
        return new ShopCeoAssignmentHistoryState(
            entity.getId(),
            entity.getShopId(),
            entity.getCeoId(),
            entity.getActionType(),
            entity.getActorAdminId(),
            entity.getCreatedAt()
        );
    }

    static ShopCeoAssignmentHistoryJpaEntity toEntity(ShopCeoAssignmentHistoryState state) {
        return ShopCeoAssignmentHistoryJpaEntity.create(
            state.shopId(),
            state.ceoId(),
            state.actionType(),
            state.actorAdminId()
        );
    }
}
