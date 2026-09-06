package com.tastyhouse.domain.product.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductImageChangeRequestId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;

public class ProductImageChangeRequest {
    private final Long id;
    private final ProductId productId;
    private final UploadedFileId imageFileId;
    private ApprovalStatus status;
    private String rejectReason;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private ProductImageChangeRequest(
        Long id,
        ProductId productId,
        UploadedFileId imageFileId,
        ApprovalStatus status,
        String rejectReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.productId = productId;
        this.imageFileId = imageFileId;
        this.status = status;
        this.rejectReason = rejectReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ProductImageChangeRequest of(ProductId productId, UploadedFileId imageFileId) {
        return new ProductImageChangeRequest(
            null,
            productId,
            imageFileId,
            ApprovalStatus.PENDING,
            null,
            null,
            null
        );
    }

    public static ProductImageChangeRequest reconstitute(
        Long id,
        ProductId productId,
        UploadedFileId imageFileId,
        ApprovalStatus status,
        String rejectReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ProductImageChangeRequest(
            id,
            productId,
            imageFileId,
            status,
            rejectReason,
            createdAt,
            updatedAt
        );
    }

    public Long getId() {
        return this.id;
    }

    public ProductId getProductId() {
        return this.productId;
    }

    public UploadedFileId getImageFileId() {
        return this.imageFileId;
    }

    public ApprovalStatus getStatus() {
        return this.status;
    }

    public String getRejectReason() {
        return this.rejectReason;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public ProductImageChangeRequestId getRequestId() {
        return ProductImageChangeRequestId.of(this.id);
    }

    public void approve() {
        requirePending();
        this.status = ApprovalStatus.APPROVED;
    }

    public void reject(String rejectReason) {
        requirePending();
        this.status = ApprovalStatus.REJECTED;
        this.rejectReason = rejectReason;
    }

    public void cancel() {
        requirePending();
        this.status = ApprovalStatus.CANCELED;
        this.rejectReason = null;
    }

    private void requirePending() {
        if (this.status != ApprovalStatus.PENDING) {
            throw new BusinessException(ErrorCode.PRODUCT_IMAGE_CHANGE_REQUEST_NOT_PENDING);
        }
    }
}
