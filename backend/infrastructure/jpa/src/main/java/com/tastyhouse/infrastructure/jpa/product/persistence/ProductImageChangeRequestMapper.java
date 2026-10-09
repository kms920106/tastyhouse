package com.tastyhouse.infrastructure.jpa.product.persistence;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.model.ProductImageChangeRequest;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;

final class ProductImageChangeRequestMapper {

    private ProductImageChangeRequestMapper() {
    }

    static ProductImageChangeRequest toDomain(ProductImageChangeRequestJpaEntity entity) {
        return ProductImageChangeRequest.reconstitute(
            entity.getId(),
            entity.getProductId() == null ? null : ProductId.of(entity.getProductId()),
            entity.getImageFileId() == null ? null : UploadedFileId.of(entity.getImageFileId()),
            entity.getStatus() == null ? null : ApprovalStatus.valueOf(entity.getStatus()),
            entity.getRejectReason(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ProductImageChangeRequestJpaEntity toEntity(ProductImageChangeRequest request) {
        return ProductImageChangeRequestJpaEntity.create(
            request.getProductId() == null ? null : request.getProductId().value(),
            request.getImageFileId() == null ? null : request.getImageFileId().value(),
            request.getStatus() == null ? null : request.getStatus().name(),
            request.getRejectReason()
        );
    }

    static void applyChanges(ProductImageChangeRequestJpaEntity entity, ProductImageChangeRequest request) {
        entity.applyChanges(
            request.getStatus() == null ? null : request.getStatus().name(),
            request.getRejectReason()
        );
    }
}
