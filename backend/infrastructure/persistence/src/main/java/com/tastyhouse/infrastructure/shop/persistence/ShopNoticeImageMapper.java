package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.ShopNoticeImage;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ShopNoticeImageMapper {
    private ShopNoticeImageMapper() {
    }

    static ShopNoticeImage toDomain(ShopNoticeImageJpaEntity entity) {
        return ShopNoticeImage.reconstitute(
            entity.getId(),
            entity.getShopNoticeId(),
            IdMapping.vo(entity.getImageFileId(), UploadedFileId::of),
            entity.getSortOrder()
        );
    }

    static ShopNoticeImageJpaEntity toEntity(ShopNoticeImage domain) {
        return ShopNoticeImageJpaEntity.create(
            domain.getShopNoticeId(),
            IdMapping.raw(domain.getImageFileId(), UploadedFileId::value),
            domain.getSortOrder()
        );
    }
}
