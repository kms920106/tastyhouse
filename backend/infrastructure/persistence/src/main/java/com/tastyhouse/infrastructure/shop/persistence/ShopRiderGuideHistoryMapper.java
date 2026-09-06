package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shop.model.ShopRiderGuideHistory;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ShopRiderGuideHistoryMapper {
    private ShopRiderGuideHistoryMapper() {
    }

    static ShopRiderGuideHistory toDomain(ShopRiderGuideHistoryJpaEntity entity) {
        return ShopRiderGuideHistory.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            entity.getActorType(),
            entity.getActorId(),
            entity.getActionType(),
            entity.getPreviousVisitGuide(),
            entity.getNewVisitGuide(),
            entity.getReason(),
            entity.getCreatedAt()
        );
    }

    static ShopRiderGuideHistoryJpaEntity toEntity(ShopRiderGuideHistory domain) {
        return ShopRiderGuideHistoryJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            domain.getActorType(),
            domain.getActorId(),
            domain.getActionType(),
            domain.getPreviousVisitGuide(),
            domain.getNewVisitGuide(),
            domain.getReason()
        );
    }
}
