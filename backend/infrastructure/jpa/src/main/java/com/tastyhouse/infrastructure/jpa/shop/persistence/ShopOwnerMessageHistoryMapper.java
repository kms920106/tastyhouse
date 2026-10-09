package com.tastyhouse.infrastructure.jpa.shop.persistence;

import com.tastyhouse.domain.shop.model.ShopOwnerMessageHistory;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ShopOwnerMessageHistoryMapper {

    private ShopOwnerMessageHistoryMapper() {
    }

    static ShopOwnerMessageHistoryJpaEntity toEntity(ShopOwnerMessageHistory shopOwnerMessageHistory) {
        return ShopOwnerMessageHistoryJpaEntity.create(
            shopOwnerMessageHistory.getShopId() == null ? null : shopOwnerMessageHistory.getShopId().value(),
            shopOwnerMessageHistory.getMessage()
        );
    }

    static ShopOwnerMessageHistory toDomain(ShopOwnerMessageHistoryJpaEntity entity) {
        return ShopOwnerMessageHistory.reconstitute(
            entity.getId(),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
            entity.getMessage(),
            entity.getCreatedAt()
        );
    }
}
