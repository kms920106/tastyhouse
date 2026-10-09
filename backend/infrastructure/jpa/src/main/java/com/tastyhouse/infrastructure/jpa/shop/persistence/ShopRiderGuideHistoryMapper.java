package com.tastyhouse.infrastructure.jpa.shop.persistence;

import com.tastyhouse.domain.shop.model.RiderGuideActionType;
import com.tastyhouse.domain.shop.model.RiderGuideActorType;
import com.tastyhouse.domain.shop.model.ShopRiderGuideHistory;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ShopRiderGuideHistoryMapper {

    private ShopRiderGuideHistoryMapper() {
    }

    static ShopRiderGuideHistory toDomain(ShopRiderGuideHistoryJpaEntity entity) {
        return ShopRiderGuideHistory.reconstitute(
            entity.getId(),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
            entity.getActorType() == null ? null : RiderGuideActorType.valueOf(entity.getActorType()),
            entity.getActorId(),
            entity.getActionType() == null ? null : RiderGuideActionType.valueOf(entity.getActionType()),
            entity.getPreviousVisitGuide(),
            entity.getNewVisitGuide(),
            entity.getReason(),
            entity.getCreatedAt()
        );
    }

    static ShopRiderGuideHistoryJpaEntity toEntity(ShopRiderGuideHistory shopRiderGuideHistory) {
        return ShopRiderGuideHistoryJpaEntity.create(
            shopRiderGuideHistory.getShopId() == null ? null : shopRiderGuideHistory.getShopId().value(),
            shopRiderGuideHistory.getActorType() == null ? null : shopRiderGuideHistory.getActorType().name(),
            shopRiderGuideHistory.getActorId(),
            shopRiderGuideHistory.getActionType() == null ? null : shopRiderGuideHistory.getActionType().name(),
            shopRiderGuideHistory.getPreviousVisitGuide(),
            shopRiderGuideHistory.getNewVisitGuide(),
            shopRiderGuideHistory.getReason()
        );
    }
}
