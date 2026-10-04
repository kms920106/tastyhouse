package com.tastyhouse.infrastructure.persistence.shop.persistence;

import com.tastyhouse.domain.shop.model.HygieneBadgeType;
import com.tastyhouse.domain.shop.model.ShopHygieneBadge;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ShopHygieneBadgeMapper {

    private ShopHygieneBadgeMapper() {
    }

    static ShopHygieneBadge toDomain(ShopHygieneBadgeJpaEntity entity) {
        return ShopHygieneBadge.reconstitute(
            entity.getId(),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
            entity.getBadgeType() == null ? null : HygieneBadgeType.valueOf(entity.getBadgeType()),
            entity.getCertifiedDate(),
            entity.getLastInspectionMonth(),
            entity.getCreatedAt()
        );
    }

    static ShopHygieneBadgeJpaEntity toEntity(ShopHygieneBadge shopHygieneBadge) {
        return ShopHygieneBadgeJpaEntity.create(
            shopHygieneBadge.getShopId() == null ? null : shopHygieneBadge.getShopId().value(),
            shopHygieneBadge.getBadgeType() == null ? null : shopHygieneBadge.getBadgeType().name(),
            shopHygieneBadge.getCertifiedDate(),
            shopHygieneBadge.getLastInspectionMonth()
        );
    }
}
