package com.tastyhouse.domain.shop.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shop.vo.ShopMenuCollectionImageId;

public class ShopMenuCollectionImage {
    private final Long id;
    private final ShopId shopId;
    private final UploadedFileId imageFileId;
    private int sort;
    private ApprovalStatus status;
    private String rejectReason;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private ShopMenuCollectionImage(
        Long id,
        ShopId shopId,
        UploadedFileId imageFileId,
        int sort,
        ApprovalStatus status,
        String rejectReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.shopId = shopId;
        this.imageFileId = imageFileId;
        this.sort = sort;
        this.status = status;
        this.rejectReason = rejectReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ShopMenuCollectionImage of(ShopId shopId, UploadedFileId imageFileId, int sort) {
        return new ShopMenuCollectionImage(
            null,
            shopId,
            imageFileId,
            sort,
            ApprovalStatus.PENDING,
            null,
            null,
            null
        );
    }

    public static ShopMenuCollectionImage reconstitute(
        Long id,
        ShopId shopId,
        UploadedFileId imageFileId,
        int sort,
        ApprovalStatus status,
        String rejectReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ShopMenuCollectionImage(
            id,
            shopId,
            imageFileId,
            sort,
            status,
            rejectReason,
            createdAt,
            updatedAt
        );
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public UploadedFileId getImageFileId() {
        return this.imageFileId;
    }

    public int getSort() {
        return this.sort;
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

    public ShopMenuCollectionImageId getImageId() {
        return ShopMenuCollectionImageId.of(this.id);
    }

    public void approve() {
        requirePending();
        this.status = ApprovalStatus.APPROVED;
        this.rejectReason = null;
    }

    public void reject(String rejectReason) {
        requirePending();
        this.status = ApprovalStatus.REJECTED;
        this.rejectReason = rejectReason;
    }

    public void changeSort(int sort) {
        this.sort = sort;
    }

    private void requirePending() {
        if (this.status != ApprovalStatus.PENDING) {
            throw new BusinessException(ErrorCode.SHOP_MENU_COLLECTION_IMAGE_NOT_PENDING);
        }
    }
}
