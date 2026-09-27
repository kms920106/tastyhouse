package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.application.shop.port.out.write.ShopNoticeState;

final class ShopNoticeMapper {
    private ShopNoticeMapper() {
    }

    static ShopNoticeState toState(ShopNoticeJpaEntity entity) {
        return new ShopNoticeState(
            entity.getId(),
            entity.getShopId(),
            entity.getContent(),
            entity.isExposed(),
            entity.isHidden(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ShopNoticeJpaEntity toEntity(ShopNoticeState state) {
        return ShopNoticeJpaEntity.create(
            state.shopId(),
            state.content(),
            state.exposed(),
            state.hidden()
        );
    }

    static void applyChanges(ShopNoticeJpaEntity entity, ShopNoticeState state) {
        entity.applyChanges(
            state.content(),
            state.exposed(),
            state.hidden()
        );
    }
}
