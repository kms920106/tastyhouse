package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shop.domain.model.ShopOwnerMessageHistory;

final class ShopOwnerMessageHistoryMapper {

    private ShopOwnerMessageHistoryMapper() {
    }

    static ShopOwnerMessageHistory toDomain(ShopOwnerMessageHistoryJpaEntity entity) {
        return ShopOwnerMessageHistory.reconstitute(
            entity.getId(),
            entity.getShopId(),
            entity.getMessage(),
            entity.getCreatedAt()
        );
    }

    static ShopOwnerMessageHistoryJpaEntity toEntity(ShopOwnerMessageHistory domain) {
        return ShopOwnerMessageHistoryJpaEntity.create(
            domain.getShopId(),
            domain.getMessage()
        );
    }
}
