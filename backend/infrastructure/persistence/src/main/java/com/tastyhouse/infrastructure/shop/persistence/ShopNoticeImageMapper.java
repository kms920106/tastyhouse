package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.application.shop.port.out.write.ShopNoticeImageState;

final class ShopNoticeImageMapper {
    private ShopNoticeImageMapper() {
    }

    static ShopNoticeImageState toState(ShopNoticeImageJpaEntity entity) {
        return new ShopNoticeImageState(
            entity.getId(),
            entity.getShopNoticeId(),
            entity.getImageFileId(),
            entity.getSortOrder()
        );
    }

    static ShopNoticeImageJpaEntity toEntity(ShopNoticeImageState state) {
        return ShopNoticeImageJpaEntity.create(
            state.shopNoticeId(),
            state.imageFileId(),
            state.sortOrder()
        );
    }
}
