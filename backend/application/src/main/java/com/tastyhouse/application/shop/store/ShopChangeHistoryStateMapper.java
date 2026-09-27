package com.tastyhouse.application.shop.store;

import com.tastyhouse.application.shop.port.out.write.ShopChangeHistoryState;
import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActorType;
import com.tastyhouse.domain.shop.model.ShopChangeCategory;
import com.tastyhouse.domain.shop.model.ShopChangeHistory;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ShopChangeHistoryStateMapper {
    private ShopChangeHistoryStateMapper() {
    }

    static ShopChangeHistory toDomain(ShopChangeHistoryState state) {
        return ShopChangeHistory.reconstitute(
            state.id(),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            state.category() == null ? null : ShopChangeCategory.valueOf(state.category()),
            state.changeType() == null ? null : ShopChangeType.valueOf(state.changeType()),
            state.actionType() == null ? null : ShopChangeActionType.valueOf(state.actionType()),
            state.actorType() == null ? null : ShopChangeActorType.valueOf(state.actorType()),
            state.actorId(),
            state.previousValue(),
            state.newValue(),
            state.createdAt()
        );
    }

    static ShopChangeHistoryState toState(ShopChangeHistory shopChangeHistory) {
        return new ShopChangeHistoryState(
            shopChangeHistory.getId(),
            shopChangeHistory.getShopId() == null ? null : shopChangeHistory.getShopId().value(),
            shopChangeHistory.getCategory() == null ? null : shopChangeHistory.getCategory().name(),
            shopChangeHistory.getChangeType() == null ? null : shopChangeHistory.getChangeType().name(),
            shopChangeHistory.getActionType() == null ? null : shopChangeHistory.getActionType().name(),
            shopChangeHistory.getActorType() == null ? null : shopChangeHistory.getActorType().name(),
            shopChangeHistory.getActorId(),
            shopChangeHistory.getPreviousValue(),
            shopChangeHistory.getNewValue(),
            shopChangeHistory.getCreatedAt()
        );
    }
}
