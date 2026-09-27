package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.application.shop.port.out.write.ShopOrderNoticeState;

final class ShopOrderNoticeMapper {
    private ShopOrderNoticeMapper() {
    }

    static ShopOrderNoticeState toState(ShopOrderNoticeJpaEntity entity) {
        return new ShopOrderNoticeState(
            entity.getId(),
            entity.getShopId(),
            entity.getContent(),
            entity.isHidden(),
            entity.getHiddenReason(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ShopOrderNoticeJpaEntity toEntity(ShopOrderNoticeState state) {
        return ShopOrderNoticeJpaEntity.create(
            state.shopId(),
            state.content(),
            state.hidden(),
            state.hiddenReason()
        );
    }

    static void applyChanges(ShopOrderNoticeJpaEntity entity, ShopOrderNoticeState state) {
        entity.applyChanges(
            state.content(),
            state.hidden(),
            state.hiddenReason()
        );
    }
}
