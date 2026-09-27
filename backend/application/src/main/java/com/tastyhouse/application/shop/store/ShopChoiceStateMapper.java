package com.tastyhouse.application.shop.store;

import com.tastyhouse.domain.shop.model.ShopChoice;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.out.write.ShopChoiceState;

final class ShopChoiceStateMapper {
    private ShopChoiceStateMapper() {
    }

    static ShopChoice toDomain(ShopChoiceState state) {
        return ShopChoice.reconstitute(
            state.id(),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            state.title(),
            state.content()
        );
    }

    static ShopChoiceState toState(ShopChoice shopChoice) {
        return new ShopChoiceState(
            shopChoice.getId(),
            shopChoice.getShopId() == null ? null : shopChoice.getShopId().value(),
            shopChoice.getTitle(),
            shopChoice.getContent()
        );
    }
}
