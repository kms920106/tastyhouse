package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.ShopBannerImage;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ShopBannerImageMapper {
    private ShopBannerImageMapper() {
    }

    static ShopBannerImage toDomain(ShopBannerImageJpaEntity entity) {
        return ShopBannerImage.reconstitute(
            entity.getId(),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
            entity.getImageFileId() == null ? null : UploadedFileId.of(entity.getImageFileId()),
            entity.getSort()
        );
    }

    static ShopBannerImageJpaEntity toEntity(ShopBannerImage shopBannerImage) {
        return ShopBannerImageJpaEntity.create(
            shopBannerImage.getShopId() == null ? null : shopBannerImage.getShopId().value(),
            shopBannerImage.getImageFileId() == null ? null : shopBannerImage.getImageFileId().value(),
            shopBannerImage.getSort()
        );
    }
}
