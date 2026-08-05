package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.ShopBannerImage;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ShopBannerImageMapper {

    private ShopBannerImageMapper() {
    }

    static ShopBannerImage toDomain(ShopBannerImageJpaEntity entity) {
        return ShopBannerImage.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            IdMapping.vo(entity.getImageFileId(), UploadedFileId::of),
            entity.getSort()
        );
    }

    static ShopBannerImageJpaEntity toEntity(ShopBannerImage domain) {
        return ShopBannerImageJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            IdMapping.raw(domain.getImageFileId(), UploadedFileId::value),
            domain.getSort()
        );
    }
}
