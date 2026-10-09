package com.tastyhouse.infrastructure.jpa.shop.persistence;

import com.tastyhouse.domain.shop.model.ShopTemporaryClosure;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ShopTemporaryClosureMapper {

    private ShopTemporaryClosureMapper() {
    }

    static ShopTemporaryClosure toDomain(ShopTemporaryClosureJpaEntity entity) {
        return ShopTemporaryClosure.reconstitute(
            entity.getId(),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
            entity.getStartDate(),
            entity.getEndDate(),
            entity.getCreatedAt()
        );
    }

    static ShopTemporaryClosureJpaEntity toEntity(ShopTemporaryClosure shopTemporaryClosure) {
        return ShopTemporaryClosureJpaEntity.create(
            shopTemporaryClosure.getShopId() == null ? null : shopTemporaryClosure.getShopId().value(),
            shopTemporaryClosure.getStartDate(),
            shopTemporaryClosure.getEndDate()
        );
    }
}
