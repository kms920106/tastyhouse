package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.shop.model.ShopCeoAssignmentActionType;
import com.tastyhouse.domain.shop.model.ShopCeoAssignmentHistory;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ShopCeoAssignmentHistoryMapper {

    private ShopCeoAssignmentHistoryMapper() {
    }

    static ShopCeoAssignmentHistory toDomain(ShopCeoAssignmentHistoryJpaEntity entity) {
        return ShopCeoAssignmentHistory.reconstitute(
            entity.getId(),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
            entity.getCeoId() == null ? null : CeoId.of(entity.getCeoId()),
            entity.getActionType() == null ? null : ShopCeoAssignmentActionType.valueOf(entity.getActionType()),
            entity.getActorAdminId(),
            entity.getCreatedAt()
        );
    }

    static ShopCeoAssignmentHistoryJpaEntity toEntity(ShopCeoAssignmentHistory shopCeoAssignmentHistory) {
        return ShopCeoAssignmentHistoryJpaEntity.create(
            shopCeoAssignmentHistory.getShopId() == null ? null : shopCeoAssignmentHistory.getShopId().value(),
            shopCeoAssignmentHistory.getCeoId() == null ? null : shopCeoAssignmentHistory.getCeoId().value(),
            shopCeoAssignmentHistory.getActionType() == null ? null : shopCeoAssignmentHistory.getActionType().name(),
            shopCeoAssignmentHistory.getActorAdminId()
        );
    }
}
