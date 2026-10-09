package com.tastyhouse.infrastructure.jpa.shop.persistence;

import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActorType;
import com.tastyhouse.domain.shop.model.ShopChangeCategory;
import com.tastyhouse.domain.shop.model.ShopChangeHistory;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ShopChangeHistoryMapper {

    private ShopChangeHistoryMapper() {
    }

    static ShopChangeHistory toDomain(ShopChangeHistoryJpaEntity entity) {
        return ShopChangeHistory.reconstitute(
            entity.getId(),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
            entity.getCategory() == null ? null : ShopChangeCategory.valueOf(entity.getCategory()),
            entity.getChangeType() == null ? null : ShopChangeType.valueOf(entity.getChangeType()),
            entity.getActionType() == null ? null : ShopChangeActionType.valueOf(entity.getActionType()),
            entity.getActorType() == null ? null : ShopChangeActorType.valueOf(entity.getActorType()),
            entity.getActorId(),
            entity.getPreviousValue(),
            entity.getNewValue(),
            entity.getCreatedAt()
        );
    }

    static ShopChangeHistoryJpaEntity toEntity(ShopChangeHistory shopChangeHistory) {
        return ShopChangeHistoryJpaEntity.create(
            shopChangeHistory.getShopId() == null ? null : shopChangeHistory.getShopId().value(),
            shopChangeHistory.getCategory() == null ? null : shopChangeHistory.getCategory().name(),
            shopChangeHistory.getChangeType() == null ? null : shopChangeHistory.getChangeType().name(),
            shopChangeHistory.getActionType() == null ? null : shopChangeHistory.getActionType().name(),
            shopChangeHistory.getActorType() == null ? null : shopChangeHistory.getActorType().name(),
            shopChangeHistory.getActorId(),
            shopChangeHistory.getPreviousValue(),
            shopChangeHistory.getNewValue()
        );
    }
}
