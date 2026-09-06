package com.tastyhouse.domain.shop.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ShopImageChangeRequest {
    private final Long id;
    private final ShopId shopId;
    private final ShopImageType imageType;
    private final UploadedFileId imageFileId;
    private ApprovalStatus status;
    private String rejectReason;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private ShopImageChangeRequest(
        Long id,
        ShopId shopId,
        ShopImageType imageType,
        UploadedFileId imageFileId,
        ApprovalStatus status,
        String rejectReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.shopId = shopId;
        this.imageType = imageType;
        this.imageFileId = imageFileId;
        this.status = status;
        this.rejectReason = rejectReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ShopImageChangeRequest of(ShopId shopId, ShopImageType imageType, UploadedFileId imageFileId) {
        return new ShopImageChangeRequest(
            null, shopId, imageType, imageFileId, ApprovalStatus.PENDING, null, null, null
        );
    }

    public static ShopImageChangeRequest reconstitute(
        Long id,
        ShopId shopId,
        ShopImageType imageType,
        UploadedFileId imageFileId,
        ApprovalStatus status,
        String rejectReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ShopImageChangeRequest(id, shopId, imageType, imageFileId, status, rejectReason, createdAt, updatedAt);
    }

    public void approve() {
        if (this.status != ApprovalStatus.PENDING) {
            throw new BusinessException(ErrorCode.SHOP_IMAGE_CHANGE_REQUEST_NOT_PENDING);
        }
        this.status = ApprovalStatus.APPROVED;
    }

    public void reject(String reason) {
        if (this.status != ApprovalStatus.PENDING) {
            throw new BusinessException(ErrorCode.SHOP_IMAGE_CHANGE_REQUEST_NOT_PENDING);
        }
        this.status = ApprovalStatus.REJECTED;
        this.rejectReason = reason;
    }

    public void cancel() {
        if (this.status != ApprovalStatus.PENDING) {
            throw new BusinessException(ErrorCode.SHOP_REQUEST_NOT_CANCELABLE);
        }
        this.status = ApprovalStatus.CANCELED;
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public ShopImageType getImageType() {
        return this.imageType;
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
}
