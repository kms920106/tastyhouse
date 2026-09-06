package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.ShopDeliveryAreaAdjustmentRequest;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ShopDeliveryAreaAdjustmentRequestMapper {
    private ShopDeliveryAreaAdjustmentRequestMapper() {
    }

    static ShopDeliveryAreaAdjustmentRequest toDomain(ShopDeliveryAreaAdjustmentRequestJpaEntity entity) {
        return ShopDeliveryAreaAdjustmentRequest.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            entity.getCounterpartShopName(),
            entity.getCounterpartBusinessNumber(),
            entity.getFranchiseName(),
            entity.getReason(),
            IdMapping.vo(entity.getConsentFileId(), UploadedFileId::of),
            entity.getStatus(),
            entity.getRejectReason(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ShopDeliveryAreaAdjustmentRequestJpaEntity toEntity(ShopDeliveryAreaAdjustmentRequest domain) {
        return ShopDeliveryAreaAdjustmentRequestJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            domain.getCounterpartShopName(),
            domain.getCounterpartBusinessNumber(),
            domain.getFranchiseName(),
            domain.getReason(),
            IdMapping.raw(domain.getConsentFileId(), UploadedFileId::value),
            domain.getStatus(),
            domain.getRejectReason()
        );
    }

    static void applyChanges(ShopDeliveryAreaAdjustmentRequestJpaEntity entity, ShopDeliveryAreaAdjustmentRequest domain) {
        entity.applyChanges(
            domain.getStatus(),
            domain.getRejectReason()
        );
    }
}
