package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.application.shop.port.out.write.ShopChoiceState;

final class ShopChoiceMapper {
    private ShopChoiceMapper() {
    }

    static ShopChoiceState toState(ShopChoiceJpaEntity entity) {
        return new ShopChoiceState(
            entity.getId(),
            entity.getShopId(),
            entity.getTitle(),
            entity.getContent()
        );
    }

    static ShopChoiceJpaEntity toEntity(ShopChoiceState state) {
        return ShopChoiceJpaEntity.create(
            state.shopId(),
            state.title(),
            state.content()
        );
    }

    static void applyChanges(ShopChoiceJpaEntity entity, ShopChoiceState state) {
        entity.applyChanges(
            state.title(),
            state.content()
        );
    }
}
