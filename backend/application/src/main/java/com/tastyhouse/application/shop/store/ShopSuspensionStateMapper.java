package com.tastyhouse.application.shop.store;

import com.tastyhouse.application.shop.port.out.write.ShopSuspensionState;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.model.ShopSuspension;
import com.tastyhouse.domain.shop.model.SuspensionReason;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ShopSuspensionStateMapper {
    private ShopSuspensionStateMapper() {
    }

    static ShopSuspension toDomain(ShopSuspensionState state) {
        return ShopSuspension.reconstitute(
            state.id(),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            state.reason() == null ? null : SuspensionReason.valueOf(state.reason()),
            state.orderMethod() == null ? null : OrderMethod.valueOf(state.orderMethod()),
            state.startAt(),
            state.endAt(),
            state.releasedAt(),
            state.createdAt(),
            state.updatedAt()
        );
    }

    static ShopSuspensionState toState(ShopSuspension shopSuspension) {
        return new ShopSuspensionState(
            shopSuspension.getId(),
            shopSuspension.getShopId() == null ? null : shopSuspension.getShopId().value(),
            shopSuspension.getReason() == null ? null : shopSuspension.getReason().name(),
            shopSuspension.getOrderMethod() == null ? null : shopSuspension.getOrderMethod().name(),
            shopSuspension.getStartAt(),
            shopSuspension.getEndAt(),
            shopSuspension.getReleasedAt(),
            shopSuspension.getCreatedAt(),
            shopSuspension.getUpdatedAt()
        );
    }
}
