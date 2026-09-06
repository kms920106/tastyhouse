package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.ShopImageChangeRequest;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ShopImageChangeRequestMapper {
    private ShopImageChangeRequestMapper() {
    }

    static ShopImageChangeRequest toDomain(ShopImageChangeRequestJpaEntity entity) {
        return ShopImageChangeRequest.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            entity.getImageType(),
            IdMapping.vo(entity.getImageFileId(), UploadedFileId::of),
            entity.getStatus(),
            entity.getRejectReason(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ShopImageChangeRequestJpaEntity toEntity(ShopImageChangeRequest domain) {
        return ShopImageChangeRequestJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            domain.getImageType(),
            IdMapping.raw(domain.getImageFileId(), UploadedFileId::value),
            domain.getStatus(),
            domain.getRejectReason()
        );
    }

    static void applyChanges(ShopImageChangeRequestJpaEntity entity, ShopImageChangeRequest domain) {
        entity.applyChanges(
            domain.getStatus(),
            domain.getRejectReason()
        );
    }
}
