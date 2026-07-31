package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shop.domain.model.ShopBannerImage;

final class ShopBannerImageMapper {

    private ShopBannerImageMapper() {
    }

    static ShopBannerImage toDomain(ShopBannerImageJpaEntity entity) {
        return ShopBannerImage.reconstitute(
            entity.getId(),
            entity.getShopId(),
            entity.getImageFileId(),
            entity.getSort()
        );
    }

    static ShopBannerImageJpaEntity toEntity(ShopBannerImage domain) {
        return ShopBannerImageJpaEntity.create(
            domain.getShopId(),
            domain.getImageFileId(),
            domain.getSort()
        );
    }
}
