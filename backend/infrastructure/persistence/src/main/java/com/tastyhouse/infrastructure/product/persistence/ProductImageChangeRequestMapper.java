package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.model.ProductImageChangeRequest;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ProductImageChangeRequestMapper {
    private ProductImageChangeRequestMapper() {
    }

    static ProductImageChangeRequest toDomain(ProductImageChangeRequestJpaEntity entity) {
        return ProductImageChangeRequest.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getProductId(), ProductId::of),
            IdMapping.vo(entity.getImageFileId(), UploadedFileId::of),
            entity.getStatus(),
            entity.getRejectReason(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ProductImageChangeRequestJpaEntity toEntity(ProductImageChangeRequest domain) {
        return ProductImageChangeRequestJpaEntity.create(
            IdMapping.raw(domain.getProductId(), ProductId::value),
            IdMapping.raw(domain.getImageFileId(), UploadedFileId::value),
            domain.getStatus(),
            domain.getRejectReason()
        );
    }

    static void applyChanges(ProductImageChangeRequestJpaEntity entity, ProductImageChangeRequest domain) {
        entity.applyChanges(
            domain.getStatus(),
            domain.getRejectReason()
        );
    }
}
