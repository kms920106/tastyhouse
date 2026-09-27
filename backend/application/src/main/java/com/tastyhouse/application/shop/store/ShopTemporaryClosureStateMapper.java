package com.tastyhouse.application.shop.store;

import com.tastyhouse.domain.shop.model.ShopTemporaryClosure;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.out.write.ShopTemporaryClosureState;

final class ShopTemporaryClosureStateMapper {
    private ShopTemporaryClosureStateMapper() {
    }

    static ShopTemporaryClosure toDomain(ShopTemporaryClosureState state) {
        return ShopTemporaryClosure.reconstitute(
            state.id(),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            state.startDate(),
            state.endDate(),
            state.createdAt()
        );
    }

    static ShopTemporaryClosureState toState(ShopTemporaryClosure shopTemporaryClosure) {
        return new ShopTemporaryClosureState(
            shopTemporaryClosure.getId(),
            shopTemporaryClosure.getShopId() == null ? null : shopTemporaryClosure.getShopId().value(),
            shopTemporaryClosure.getStartDate(),
            shopTemporaryClosure.getEndDate(),
            shopTemporaryClosure.getCreatedAt()
        );
    }
}
