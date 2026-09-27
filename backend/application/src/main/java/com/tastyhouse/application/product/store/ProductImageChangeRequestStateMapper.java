package com.tastyhouse.application.product.store;

import com.tastyhouse.application.product.port.out.write.ProductImageChangeRequestState;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.model.ProductImageChangeRequest;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;

final class ProductImageChangeRequestStateMapper {
    private ProductImageChangeRequestStateMapper() {
    }

    static ProductImageChangeRequest toDomain(ProductImageChangeRequestState state) {
        return ProductImageChangeRequest.reconstitute(
            state.id(),
            state.productId() == null ? null : ProductId.of(state.productId()),
            state.imageFileId() == null ? null : UploadedFileId.of(state.imageFileId()),
            state.status() == null ? null : ApprovalStatus.valueOf(state.status()),
            state.rejectReason(),
            state.createdAt(),
            state.updatedAt()
        );
    }

    static ProductImageChangeRequestState toState(ProductImageChangeRequest request) {
        return new ProductImageChangeRequestState(
            request.getId(),
            request.getProductId() == null ? null : request.getProductId().value(),
            request.getImageFileId() == null ? null : request.getImageFileId().value(),
            request.getStatus() == null ? null : request.getStatus().name(),
            request.getRejectReason(),
            request.getCreatedAt(),
            request.getUpdatedAt()
        );
    }
}
