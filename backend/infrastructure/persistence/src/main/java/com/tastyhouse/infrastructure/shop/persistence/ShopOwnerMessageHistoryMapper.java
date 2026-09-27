package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.application.shop.port.out.write.ShopOwnerMessageHistoryState;

final class ShopOwnerMessageHistoryMapper {
    private ShopOwnerMessageHistoryMapper() {
    }

    static ShopOwnerMessageHistoryJpaEntity toEntity(ShopOwnerMessageHistoryState state) {
        return ShopOwnerMessageHistoryJpaEntity.create(
            state.shopId(),
            state.message()
        );
    }

    static ShopOwnerMessageHistoryState toState(ShopOwnerMessageHistoryJpaEntity entity) {
        return new ShopOwnerMessageHistoryState(
            entity.getId(),
            entity.getShopId(),
            entity.getMessage(),
            entity.getCreatedAt()
        );
    }
}
