package com.tastyhouse.application.shop.store;

import com.tastyhouse.domain.shop.model.ShopOwnerMessageHistory;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.out.write.ShopOwnerMessageHistoryState;

final class ShopOwnerMessageHistoryStateMapper {
    private ShopOwnerMessageHistoryStateMapper() {
    }

    static ShopOwnerMessageHistory toDomain(ShopOwnerMessageHistoryState state) {
        return ShopOwnerMessageHistory.reconstitute(
            state.id(),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            state.message(),
            state.createdAt()
        );
    }

    static ShopOwnerMessageHistoryState toState(ShopOwnerMessageHistory shopOwnerMessageHistory) {
        return new ShopOwnerMessageHistoryState(
            shopOwnerMessageHistory.getId(),
            shopOwnerMessageHistory.getShopId() == null ? null : shopOwnerMessageHistory.getShopId().value(),
            shopOwnerMessageHistory.getMessage(),
            shopOwnerMessageHistory.getCreatedAt()
        );
    }
}
