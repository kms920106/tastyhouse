package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.ShopRequestIndex;
import com.tastyhouse.domain.shop.model.ShopRequestStatus;
import com.tastyhouse.domain.shop.model.ShopRequestType;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ShopRequestIndexMapper {
    private ShopRequestIndexMapper() {
    }

    static ShopRequestIndex toDomain(ShopRequestIndexJpaEntity entity) {
        return ShopRequestIndex.reconstitute(
            entity.getId(),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
            entity.getRequestType() == null ? null : ShopRequestType.valueOf(entity.getRequestType()),
            entity.getSourceRequestId(),
            entity.getSummary(),
            entity.getStatus() == null ? null : ShopRequestStatus.valueOf(entity.getStatus()),
            entity.getRejectReason(),
            entity.getAttachmentFileId() == null ? null : UploadedFileId.of(entity.getAttachmentFileId()),
            entity.getRequestedByCeoId(),
            entity.getProcessedAt(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ShopRequestIndexJpaEntity toEntity(ShopRequestIndex shopRequestIndex) {
        return ShopRequestIndexJpaEntity.create(
            shopRequestIndex.getShopId() == null ? null : shopRequestIndex.getShopId().value(),
            shopRequestIndex.getRequestType() == null ? null : shopRequestIndex.getRequestType().name(),
            shopRequestIndex.getSourceRequestId(),
            shopRequestIndex.getSummary(),
            shopRequestIndex.getStatus() == null ? null : shopRequestIndex.getStatus().name(),
            shopRequestIndex.getRejectReason(),
            shopRequestIndex.getAttachmentFileId() == null ? null : shopRequestIndex.getAttachmentFileId().value(),
            shopRequestIndex.getRequestedByCeoId(),
            shopRequestIndex.getProcessedAt()
        );
    }

    static void applyChanges(ShopRequestIndexJpaEntity entity, ShopRequestIndex shopRequestIndex) {
        entity.applyChanges(shopRequestIndex.getStatus() == null ? null : shopRequestIndex.getStatus().name(), shopRequestIndex.getRejectReason(), shopRequestIndex.getProcessedAt());
    }
}
