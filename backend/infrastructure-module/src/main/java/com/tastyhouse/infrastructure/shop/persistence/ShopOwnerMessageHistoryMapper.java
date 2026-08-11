package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shop.model.ShopOwnerMessageHistory;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ShopOwnerMessageHistoryMapper {

    private ShopOwnerMessageHistoryMapper() {
    }

    static ShopOwnerMessageHistoryJpaEntity toEntity(ShopOwnerMessageHistory domain) {
        return ShopOwnerMessageHistoryJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            domain.getMessage()
        );
    }

    static ShopOwnerMessageHistory toDomain(ShopOwnerMessageHistoryJpaEntity entity) {
        return ShopOwnerMessageHistory.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            entity.getMessage(),
            entity.getCreatedAt()
        );
    }
}
