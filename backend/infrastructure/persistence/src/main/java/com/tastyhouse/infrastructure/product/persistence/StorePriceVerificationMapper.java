package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.model.StorePriceVerification;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class StorePriceVerificationMapper {
    private StorePriceVerificationMapper() {
    }

    static StorePriceVerification toDomain(StorePriceVerificationJpaEntity entity) {
        return StorePriceVerification.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            IdMapping.vo(entity.getPriceListFileId(), UploadedFileId::of),
            entity.getStatus(),
            entity.getRejectReason(),
            entity.getRequestedByCeoId(),
            entity.getProcessedAt(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static StorePriceVerificationJpaEntity toEntity(StorePriceVerification domain) {
        return StorePriceVerificationJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            IdMapping.raw(domain.getPriceListFileId(), UploadedFileId::value),
            domain.getStatus(),
            domain.getRejectReason(),
            domain.getRequestedByCeoId(),
            domain.getProcessedAt()
        );
    }

    static void applyChanges(StorePriceVerificationJpaEntity entity, StorePriceVerification domain) {
        entity.applyChanges(
            domain.getStatus(),
            domain.getRejectReason(),
            domain.getProcessedAt()
        );
    }
}
