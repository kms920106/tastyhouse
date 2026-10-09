package com.tastyhouse.infrastructure.jpa.shop.persistence;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.ShopNoticeImage;

final class ShopNoticeImageMapper {

    private ShopNoticeImageMapper() {
    }

    static ShopNoticeImage toDomain(ShopNoticeImageJpaEntity entity) {
        return ShopNoticeImage.reconstitute(
            entity.getId(),
            entity.getShopNoticeId(),
            entity.getImageFileId() == null ? null : UploadedFileId.of(entity.getImageFileId()),
            entity.getSortOrder()
        );
    }

    static ShopNoticeImageJpaEntity toEntity(ShopNoticeImage shopNoticeImage) {
        return ShopNoticeImageJpaEntity.create(
            shopNoticeImage.getShopNoticeId(),
            shopNoticeImage.getImageFileId() == null ? null : shopNoticeImage.getImageFileId().value(),
            shopNoticeImage.getSortOrder()
        );
    }
}
