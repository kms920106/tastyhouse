package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.application.shop.port.out.write.ShopBookmarkState;

final class ShopBookmarkMapper {
    private ShopBookmarkMapper() {
    }

    static ShopBookmarkState toState(ShopBookmarkJpaEntity entity) {
        return new ShopBookmarkState(
            entity.getId(),
            entity.getShopId(),
            entity.getMemberId()
        );
    }

    static ShopBookmarkJpaEntity toEntity(ShopBookmarkState state) {
        return ShopBookmarkJpaEntity.create(
            state.shopId(),
            state.memberId()
        );
    }
}
