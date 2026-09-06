package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shop.model.ShopTemporaryClosure;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ShopTemporaryClosureMapper {
    private ShopTemporaryClosureMapper() {
    }

    static ShopTemporaryClosure toDomain(ShopTemporaryClosureJpaEntity entity) {
        return ShopTemporaryClosure.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            entity.getStartDate(),
            entity.getEndDate(),
            entity.getCreatedAt()
        );
    }

    static ShopTemporaryClosureJpaEntity toEntity(ShopTemporaryClosure domain) {
        return ShopTemporaryClosureJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            domain.getStartDate(),
            domain.getEndDate()
        );
    }
}
