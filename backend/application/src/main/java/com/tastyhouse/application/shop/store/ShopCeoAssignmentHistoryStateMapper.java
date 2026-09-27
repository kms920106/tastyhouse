package com.tastyhouse.application.shop.store;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.shop.model.ShopCeoAssignmentActionType;
import com.tastyhouse.domain.shop.model.ShopCeoAssignmentHistory;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.out.write.ShopCeoAssignmentHistoryState;

final class ShopCeoAssignmentHistoryStateMapper {
    private ShopCeoAssignmentHistoryStateMapper() {
    }

    static ShopCeoAssignmentHistory toDomain(ShopCeoAssignmentHistoryState state) {
        return ShopCeoAssignmentHistory.reconstitute(
            state.id(),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            state.ceoId() == null ? null : CeoId.of(state.ceoId()),
            state.actionType() == null ? null : ShopCeoAssignmentActionType.valueOf(state.actionType()),
            state.actorAdminId(),
            state.createdAt()
        );
    }

    static ShopCeoAssignmentHistoryState toState(ShopCeoAssignmentHistory shopCeoAssignmentHistory) {
        return new ShopCeoAssignmentHistoryState(
            shopCeoAssignmentHistory.getId(),
            shopCeoAssignmentHistory.getShopId() == null ? null : shopCeoAssignmentHistory.getShopId().value(),
            shopCeoAssignmentHistory.getCeoId() == null ? null : shopCeoAssignmentHistory.getCeoId().value(),
            shopCeoAssignmentHistory.getActionType() == null ? null : shopCeoAssignmentHistory.getActionType().name(),
            shopCeoAssignmentHistory.getActorAdminId(),
            shopCeoAssignmentHistory.getCreatedAt()
        );
    }
}
