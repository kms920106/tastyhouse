package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.ShopMenuCollectionImage;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ShopMenuCollectionImageMapper {
    private ShopMenuCollectionImageMapper() {
    }

    static ShopMenuCollectionImage toDomain(ShopMenuCollectionImageJpaEntity entity) {
        return ShopMenuCollectionImage.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            IdMapping.vo(entity.getImageFileId(), UploadedFileId::of),
            entity.getSort(),
            entity.getStatus(),
            entity.getRejectReason(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ShopMenuCollectionImageJpaEntity toEntity(ShopMenuCollectionImage domain) {
        return ShopMenuCollectionImageJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            IdMapping.raw(domain.getImageFileId(), UploadedFileId::value),
            domain.getSort(),
            domain.getStatus(),
            domain.getRejectReason()
        );
    }

    static void applyChanges(ShopMenuCollectionImageJpaEntity entity, ShopMenuCollectionImage domain) {
        entity.applyChanges(
            domain.getSort(),
            domain.getStatus(),
            domain.getRejectReason()
        );
    }
}
