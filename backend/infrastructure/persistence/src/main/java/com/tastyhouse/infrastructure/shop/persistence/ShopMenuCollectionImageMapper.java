package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.application.shop.port.out.write.ShopMenuCollectionImageState;

final class ShopMenuCollectionImageMapper {
    private ShopMenuCollectionImageMapper() {
    }

    static ShopMenuCollectionImageState toState(ShopMenuCollectionImageJpaEntity entity) {
        return new ShopMenuCollectionImageState(
            entity.getId(),
            entity.getShopId(),
            entity.getImageFileId(),
            entity.getSort(),
            entity.getStatus(),
            entity.getRejectReason(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ShopMenuCollectionImageJpaEntity toEntity(ShopMenuCollectionImageState state) {
        return ShopMenuCollectionImageJpaEntity.create(
            state.shopId(),
            state.imageFileId(),
            state.sort(),
            state.status(),
            state.rejectReason()
        );
    }

    static void applyChanges(ShopMenuCollectionImageJpaEntity entity, ShopMenuCollectionImageState state) {
        entity.applyChanges(
            state.sort(),
            state.status(),
            state.rejectReason()
        );
    }
}
