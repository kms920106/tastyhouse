package com.tastyhouse.application.shop.store;

import com.tastyhouse.application.shop.port.out.write.ShopOrderMethodState;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.model.ShopOrderMethod;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ShopOrderMethodStateMapper {
    private ShopOrderMethodStateMapper() {
    }

    static ShopOrderMethod toDomain(ShopOrderMethodState state) {
        return ShopOrderMethod.reconstitute(
            state.id(),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            state.orderMethod() == null ? null : OrderMethod.valueOf(state.orderMethod())
        );
    }

    static ShopOrderMethodState toState(ShopOrderMethod shopOrderMethod) {
        return new ShopOrderMethodState(
            shopOrderMethod.getId(),
            shopOrderMethod.getShopId() == null ? null : shopOrderMethod.getShopId().value(),
            shopOrderMethod.getOrderMethod() == null ? null : shopOrderMethod.getOrderMethod().name()
        );
    }
}
