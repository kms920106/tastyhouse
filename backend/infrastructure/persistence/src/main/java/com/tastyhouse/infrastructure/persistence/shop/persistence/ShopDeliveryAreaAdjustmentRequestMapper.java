package com.tastyhouse.infrastructure.persistence.shop.persistence;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.DeliveryAreaAdjustmentStatus;
import com.tastyhouse.domain.shop.model.ShopDeliveryAreaAdjustmentRequest;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ShopDeliveryAreaAdjustmentRequestMapper {

    private ShopDeliveryAreaAdjustmentRequestMapper() {
    }

    static ShopDeliveryAreaAdjustmentRequest toDomain(ShopDeliveryAreaAdjustmentRequestJpaEntity entity) {
        return ShopDeliveryAreaAdjustmentRequest.reconstitute(
            entity.getId(),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
            entity.getCounterpartShopName(),
            entity.getCounterpartBusinessNumber(),
            entity.getFranchiseName(),
            entity.getReason(),
            entity.getConsentFileId() == null ? null : UploadedFileId.of(entity.getConsentFileId()),
            entity.getStatus() == null ? null : DeliveryAreaAdjustmentStatus.valueOf(entity.getStatus()),
            entity.getRejectReason(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ShopDeliveryAreaAdjustmentRequestJpaEntity toEntity(ShopDeliveryAreaAdjustmentRequest shopDeliveryAreaAdjustmentRequest) {
        return ShopDeliveryAreaAdjustmentRequestJpaEntity.create(
            shopDeliveryAreaAdjustmentRequest.getShopId() == null ? null : shopDeliveryAreaAdjustmentRequest.getShopId().value(),
            shopDeliveryAreaAdjustmentRequest.getCounterpartShopName(),
            shopDeliveryAreaAdjustmentRequest.getCounterpartBusinessNumber(),
            shopDeliveryAreaAdjustmentRequest.getFranchiseName(),
            shopDeliveryAreaAdjustmentRequest.getReason(),
            shopDeliveryAreaAdjustmentRequest.getConsentFileId() == null ? null : shopDeliveryAreaAdjustmentRequest.getConsentFileId().value(),
            shopDeliveryAreaAdjustmentRequest.getStatus() == null ? null : shopDeliveryAreaAdjustmentRequest.getStatus().name(),
            shopDeliveryAreaAdjustmentRequest.getRejectReason()
        );
    }

    static void applyChanges(ShopDeliveryAreaAdjustmentRequestJpaEntity entity, ShopDeliveryAreaAdjustmentRequest shopDeliveryAreaAdjustmentRequest) {
        entity.applyChanges(
            shopDeliveryAreaAdjustmentRequest.getStatus() == null ? null : shopDeliveryAreaAdjustmentRequest.getStatus().name(),
            shopDeliveryAreaAdjustmentRequest.getRejectReason()
        );
    }
}
