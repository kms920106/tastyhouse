package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.application.shop.port.out.write.ShopImageChangeRequestState;

final class ShopImageChangeRequestMapper {
    private ShopImageChangeRequestMapper() {
    }

    static ShopImageChangeRequestState toState(ShopImageChangeRequestJpaEntity entity) {
        return new ShopImageChangeRequestState(
            entity.getId(),
            entity.getShopId(),
            entity.getImageType(),
            entity.getImageFileId(),
            entity.getStatus(),
            entity.getRejectReason(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ShopImageChangeRequestJpaEntity toEntity(ShopImageChangeRequestState state) {
        return ShopImageChangeRequestJpaEntity.create(
            state.shopId(),
            state.imageType(),
            state.imageFileId(),
            state.status(),
            state.rejectReason()
        );
    }

    static void applyChanges(ShopImageChangeRequestJpaEntity entity, ShopImageChangeRequestState state) {
        entity.applyChanges(
            state.status(),
            state.rejectReason()
        );
    }
}
