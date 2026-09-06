package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shop.model.ShopOriginInfo;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ShopOriginInfoMapper {
    private ShopOriginInfoMapper() {
    }

    static ShopOriginInfo toDomain(ShopOriginInfoJpaEntity entity) {
        return ShopOriginInfo.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            entity.getSourceType(),
            entity.getContent(),
            entity.getUrl(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ShopOriginInfoJpaEntity toEntity(ShopOriginInfo domain) {
        return ShopOriginInfoJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            domain.getSourceType(),
            domain.getContent(),
            domain.getUrl()
        );
    }

    static void applyChanges(ShopOriginInfoJpaEntity entity, ShopOriginInfo domain) {
        entity.applyChanges(domain.getSourceType(), domain.getContent(), domain.getUrl());
    }
}
