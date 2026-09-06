package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.shop.model.ShopCeoAssignmentHistory;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ShopCeoAssignmentHistoryMapper {
    private ShopCeoAssignmentHistoryMapper() {
    }

    static ShopCeoAssignmentHistory toDomain(ShopCeoAssignmentHistoryJpaEntity entity) {
        return ShopCeoAssignmentHistory.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            IdMapping.vo(entity.getCeoId(), CeoId::of),
            entity.getActionType(),
            entity.getActorAdminId(),
            entity.getCreatedAt()
        );
    }

    static ShopCeoAssignmentHistoryJpaEntity toEntity(ShopCeoAssignmentHistory domain) {
        return ShopCeoAssignmentHistoryJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            IdMapping.raw(domain.getCeoId(), CeoId::value),
            domain.getActionType(),
            domain.getActorAdminId()
        );
    }
}
