package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shop.model.ShopHygieneBadge;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ShopHygieneBadgeMapper {
    private ShopHygieneBadgeMapper() {
    }

    static ShopHygieneBadge toDomain(ShopHygieneBadgeJpaEntity entity) {
        return ShopHygieneBadge.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            entity.getBadgeType(),
            entity.getCertifiedDate(),
            entity.getLastInspectionMonth(),
            entity.getCreatedAt()
        );
    }

    static ShopHygieneBadgeJpaEntity toEntity(ShopHygieneBadge domain) {
        return ShopHygieneBadgeJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            domain.getBadgeType(),
            domain.getCertifiedDate(),
            domain.getLastInspectionMonth()
        );
    }
}
