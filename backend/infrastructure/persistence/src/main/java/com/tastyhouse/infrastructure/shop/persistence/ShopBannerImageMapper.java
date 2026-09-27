package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.application.shop.port.out.write.ShopBannerImageState;

final class ShopBannerImageMapper {
    private ShopBannerImageMapper() {
    }

    static ShopBannerImageState toState(ShopBannerImageJpaEntity entity) {
        return new ShopBannerImageState(
            entity.getId(),
            entity.getShopId(),
            entity.getImageFileId(),
            entity.getSort()
        );
    }

    static ShopBannerImageJpaEntity toEntity(ShopBannerImageState state) {
        return ShopBannerImageJpaEntity.create(
            state.shopId(),
            state.imageFileId(),
            state.sort()
        );
    }
}
