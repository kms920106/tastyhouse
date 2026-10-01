package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shop.model.ShopMenuCollectionImage;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ShopMenuCollectionImageMapper {

    private ShopMenuCollectionImageMapper() {
    }

    static ShopMenuCollectionImage toDomain(ShopMenuCollectionImageJpaEntity entity) {
        return ShopMenuCollectionImage.reconstitute(
            entity.getId(),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
            entity.getImageFileId() == null ? null : UploadedFileId.of(entity.getImageFileId()),
            entity.getSort(),
            entity.getStatus() == null ? null : ApprovalStatus.valueOf(entity.getStatus()),
            entity.getRejectReason(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ShopMenuCollectionImageJpaEntity toEntity(ShopMenuCollectionImage shopMenuCollectionImage) {
        return ShopMenuCollectionImageJpaEntity.create(
            shopMenuCollectionImage.getShopId() == null ? null : shopMenuCollectionImage.getShopId().value(),
            shopMenuCollectionImage.getImageFileId() == null ? null : shopMenuCollectionImage.getImageFileId().value(),
            shopMenuCollectionImage.getSort(),
            shopMenuCollectionImage.getStatus() == null ? null : shopMenuCollectionImage.getStatus().name(),
            shopMenuCollectionImage.getRejectReason()
        );
    }

    static void applyChanges(ShopMenuCollectionImageJpaEntity entity, ShopMenuCollectionImage shopMenuCollectionImage) {
        entity.applyChanges(
            shopMenuCollectionImage.getSort(),
            shopMenuCollectionImage.getStatus() == null ? null : shopMenuCollectionImage.getStatus().name(),
            shopMenuCollectionImage.getRejectReason()
        );
    }
}
