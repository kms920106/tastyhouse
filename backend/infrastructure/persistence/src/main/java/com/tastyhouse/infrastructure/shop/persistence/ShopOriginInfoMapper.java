package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shop.model.OriginSourceType;
import com.tastyhouse.domain.shop.model.ShopOriginInfo;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ShopOriginInfoMapper {
    private ShopOriginInfoMapper() {
    }

    static ShopOriginInfo toDomain(ShopOriginInfoJpaEntity entity) {
        return ShopOriginInfo.reconstitute(
            entity.getId(),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
            entity.getSourceType() == null ? null : OriginSourceType.valueOf(entity.getSourceType()),
            entity.getContent(),
            entity.getUrl(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ShopOriginInfoJpaEntity toEntity(ShopOriginInfo shopOriginInfo) {
        return ShopOriginInfoJpaEntity.create(
            shopOriginInfo.getShopId() == null ? null : shopOriginInfo.getShopId().value(),
            shopOriginInfo.getSourceType() == null ? null : shopOriginInfo.getSourceType().name(),
            shopOriginInfo.getContent(),
            shopOriginInfo.getUrl()
        );
    }

    static void applyChanges(ShopOriginInfoJpaEntity entity, ShopOriginInfo shopOriginInfo) {
        entity.applyChanges(shopOriginInfo.getSourceType() == null ? null : shopOriginInfo.getSourceType().name(), shopOriginInfo.getContent(), shopOriginInfo.getUrl());
    }
}
