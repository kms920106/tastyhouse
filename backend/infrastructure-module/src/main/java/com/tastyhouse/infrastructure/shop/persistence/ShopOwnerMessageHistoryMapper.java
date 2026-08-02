package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shop.model.ShopOwnerMessageHistory;

final class ShopOwnerMessageHistoryMapper {

    private ShopOwnerMessageHistoryMapper() {
    }

    static ShopOwnerMessageHistoryJpaEntity toEntity(ShopOwnerMessageHistory domain) {
        return ShopOwnerMessageHistoryJpaEntity.create(
            domain.getShopId(),
            domain.getMessage()
        );
    }
}
