package com.tastyhouse.infrastructure.persistence.product.persistence;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.model.StorePriceVerification;
import com.tastyhouse.domain.product.model.StorePriceVerificationStatus;
import com.tastyhouse.domain.shop.vo.ShopId;

final class StorePriceVerificationMapper {

    private StorePriceVerificationMapper() {
    }

    static StorePriceVerification toDomain(StorePriceVerificationJpaEntity entity) {
        return StorePriceVerification.reconstitute(
            entity.getId(),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
            entity.getPriceListFileId() == null ? null : UploadedFileId.of(entity.getPriceListFileId()),
            entity.getStatus() == null ? null : StorePriceVerificationStatus.valueOf(entity.getStatus()),
            entity.getRejectReason(),
            entity.getRequestedByCeoId(),
            entity.getProcessedAt(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static StorePriceVerificationJpaEntity toEntity(StorePriceVerification verification) {
        return StorePriceVerificationJpaEntity.create(
            verification.getShopId() == null ? null : verification.getShopId().value(),
            verification.getPriceListFileId() == null ? null : verification.getPriceListFileId().value(),
            verification.getStatus() == null ? null : verification.getStatus().name(),
            verification.getRejectReason(),
            verification.getRequestedByCeoId(),
            verification.getProcessedAt()
        );
    }

    static void applyChanges(StorePriceVerificationJpaEntity entity, StorePriceVerification verification) {
        entity.applyChanges(
            verification.getStatus() == null ? null : verification.getStatus().name(),
            verification.getRejectReason(),
            verification.getProcessedAt()
        );
    }
}
