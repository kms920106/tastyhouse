package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.ShopRequestIndex;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ShopRequestIndexMapper {
    private ShopRequestIndexMapper() {
    }

    static ShopRequestIndex toDomain(ShopRequestIndexJpaEntity entity) {
        return ShopRequestIndex.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            entity.getRequestType(),
            entity.getSourceRequestId(),
            entity.getSummary(),
            entity.getStatus(),
            entity.getRejectReason(),
            IdMapping.vo(entity.getAttachmentFileId(), UploadedFileId::of),
            entity.getRequestedByCeoId(),
            entity.getProcessedAt(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ShopRequestIndexJpaEntity toEntity(ShopRequestIndex domain) {
        return ShopRequestIndexJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            domain.getRequestType(),
            domain.getSourceRequestId(),
            domain.getSummary(),
            domain.getStatus(),
            domain.getRejectReason(),
            IdMapping.raw(domain.getAttachmentFileId(), UploadedFileId::value),
            domain.getRequestedByCeoId(),
            domain.getProcessedAt()
        );
    }

    static void applyChanges(ShopRequestIndexJpaEntity entity, ShopRequestIndex domain) {
        entity.applyChanges(domain.getStatus(), domain.getRejectReason(), domain.getProcessedAt());
    }
}
