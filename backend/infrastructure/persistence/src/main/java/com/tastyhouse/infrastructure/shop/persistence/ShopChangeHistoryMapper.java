package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shop.model.ShopChangeHistory;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ShopChangeHistoryMapper {
    private ShopChangeHistoryMapper() {
    }

    static ShopChangeHistory toDomain(ShopChangeHistoryJpaEntity entity) {
        return ShopChangeHistory.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            entity.getCategory(),
            entity.getChangeType(),
            entity.getActionType(),
            entity.getActorType(),
            entity.getActorId(),
            entity.getPreviousValue(),
            entity.getNewValue(),
            entity.getCreatedAt()
        );
    }

    static ShopChangeHistoryJpaEntity toEntity(ShopChangeHistory domain) {
        return ShopChangeHistoryJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            domain.getCategory(),
            domain.getChangeType(),
            domain.getActionType(),
            domain.getActorType(),
            domain.getActorId(),
            domain.getPreviousValue(),
            domain.getNewValue()
        );
    }
}
