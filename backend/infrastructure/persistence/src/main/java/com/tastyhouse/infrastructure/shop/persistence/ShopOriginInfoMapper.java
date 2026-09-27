package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.application.shop.port.out.write.ShopOriginInfoState;

final class ShopOriginInfoMapper {
    private ShopOriginInfoMapper() {
    }

    static ShopOriginInfoState toState(ShopOriginInfoJpaEntity entity) {
        return new ShopOriginInfoState(
            entity.getId(),
            entity.getShopId(),
            entity.getSourceType(),
            entity.getContent(),
            entity.getUrl(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ShopOriginInfoJpaEntity toEntity(ShopOriginInfoState state) {
        return ShopOriginInfoJpaEntity.create(
            state.shopId(),
            state.sourceType(),
            state.content(),
            state.url()
        );
    }

    static void applyChanges(ShopOriginInfoJpaEntity entity, ShopOriginInfoState state) {
        entity.applyChanges(state.sourceType(), state.content(), state.url());
    }
}
