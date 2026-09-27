package com.tastyhouse.application.shop.store;

import com.tastyhouse.domain.shop.model.RiderGuideActionType;
import com.tastyhouse.domain.shop.model.RiderGuideActorType;
import com.tastyhouse.domain.shop.model.ShopRiderGuideHistory;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.out.write.ShopRiderGuideHistoryState;

final class ShopRiderGuideHistoryStateMapper {
    private ShopRiderGuideHistoryStateMapper() {
    }

    static ShopRiderGuideHistory toDomain(ShopRiderGuideHistoryState state) {
        return ShopRiderGuideHistory.reconstitute(
            state.id(),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            state.actorType() == null ? null : RiderGuideActorType.valueOf(state.actorType()),
            state.actorId(),
            state.actionType() == null ? null : RiderGuideActionType.valueOf(state.actionType()),
            state.previousVisitGuide(),
            state.newVisitGuide(),
            state.reason(),
            state.createdAt()
        );
    }

    static ShopRiderGuideHistoryState toState(ShopRiderGuideHistory shopRiderGuideHistory) {
        return new ShopRiderGuideHistoryState(
            shopRiderGuideHistory.getId(),
            shopRiderGuideHistory.getShopId() == null ? null : shopRiderGuideHistory.getShopId().value(),
            shopRiderGuideHistory.getActorType() == null ? null : shopRiderGuideHistory.getActorType().name(),
            shopRiderGuideHistory.getActorId(),
            shopRiderGuideHistory.getActionType() == null ? null : shopRiderGuideHistory.getActionType().name(),
            shopRiderGuideHistory.getPreviousVisitGuide(),
            shopRiderGuideHistory.getNewVisitGuide(),
            shopRiderGuideHistory.getReason(),
            shopRiderGuideHistory.getCreatedAt()
        );
    }
}
