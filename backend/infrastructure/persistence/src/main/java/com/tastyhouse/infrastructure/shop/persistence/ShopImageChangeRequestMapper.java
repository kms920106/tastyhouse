package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shop.model.ShopImageChangeRequest;
import com.tastyhouse.domain.shop.model.ShopImageType;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ShopImageChangeRequestMapper {

    private ShopImageChangeRequestMapper() {
    }

    static ShopImageChangeRequest toDomain(ShopImageChangeRequestJpaEntity entity) {
        return ShopImageChangeRequest.reconstitute(
            entity.getId(),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
            entity.getImageType() == null ? null : ShopImageType.valueOf(entity.getImageType()),
            entity.getImageFileId() == null ? null : UploadedFileId.of(entity.getImageFileId()),
            entity.getStatus() == null ? null : ApprovalStatus.valueOf(entity.getStatus()),
            entity.getRejectReason(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ShopImageChangeRequestJpaEntity toEntity(ShopImageChangeRequest shopImageChangeRequest) {
        return ShopImageChangeRequestJpaEntity.create(
            shopImageChangeRequest.getShopId() == null ? null : shopImageChangeRequest.getShopId().value(),
            shopImageChangeRequest.getImageType() == null ? null : shopImageChangeRequest.getImageType().name(),
            shopImageChangeRequest.getImageFileId() == null ? null : shopImageChangeRequest.getImageFileId().value(),
            shopImageChangeRequest.getStatus() == null ? null : shopImageChangeRequest.getStatus().name(),
            shopImageChangeRequest.getRejectReason()
        );
    }

    static void applyChanges(ShopImageChangeRequestJpaEntity entity, ShopImageChangeRequest shopImageChangeRequest) {
        entity.applyChanges(
            shopImageChangeRequest.getStatus() == null ? null : shopImageChangeRequest.getStatus().name(),
            shopImageChangeRequest.getRejectReason()
        );
    }
}
