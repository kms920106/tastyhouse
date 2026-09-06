package com.tastyhouse.domain.shop.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ShopRequestIndex {
    private final Long id;
    private final ShopId shopId;
    private final ShopRequestType requestType;
    private final Long sourceRequestId;
    private final String summary;
    private ShopRequestStatus status;
    private String rejectReason;
    private final UploadedFileId attachmentFileId;
    private final Long requestedByCeoId;
    private LocalDateTime processedAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private ShopRequestIndex(
        Long id,
        ShopId shopId,
        ShopRequestType requestType,
        Long sourceRequestId,
        String summary,
        ShopRequestStatus status,
        String rejectReason,
        UploadedFileId attachmentFileId,
        Long requestedByCeoId,
        LocalDateTime processedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.shopId = shopId;
        this.requestType = requestType;
        this.sourceRequestId = sourceRequestId;
        this.summary = summary;
        this.status = status;
        this.rejectReason = rejectReason;
        this.attachmentFileId = attachmentFileId;
        this.requestedByCeoId = requestedByCeoId;
        this.processedAt = processedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ShopRequestIndex of(
        ShopId shopId,
        ShopRequestType requestType,
        Long sourceRequestId,
        String summary,
        UploadedFileId attachmentFileId,
        Long requestedByCeoId
    ) {
        return new ShopRequestIndex(
            null,
            shopId,
            requestType,
            sourceRequestId,
            summary,
            ShopRequestStatus.PENDING,
            null,
            attachmentFileId,
            requestedByCeoId,
            null,
            null,
            null
        );
    }

    public static ShopRequestIndex reconstitute(
        Long id,
        ShopId shopId,
        ShopRequestType requestType,
        Long sourceRequestId,
        String summary,
        ShopRequestStatus status,
        String rejectReason,
        UploadedFileId attachmentFileId,
        Long requestedByCeoId,
        LocalDateTime processedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ShopRequestIndex(
            id,
            shopId,
            requestType,
            sourceRequestId,
            summary,
            status,
            rejectReason,
            attachmentFileId,
            requestedByCeoId,
            processedAt,
            createdAt,
            updatedAt
        );
    }

    public void syncStatus(ShopRequestStatus status, String rejectReason, LocalDateTime processedAt) {
        this.status = status;
        this.rejectReason = rejectReason;
        this.processedAt = processedAt;
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public ShopRequestType getRequestType() {
        return this.requestType;
    }

    public Long getSourceRequestId() {
        return this.sourceRequestId;
    }

    public String getSummary() {
        return this.summary;
    }

    public ShopRequestStatus getStatus() {
        return this.status;
    }

    public String getRejectReason() {
        return this.rejectReason;
    }

    public UploadedFileId getAttachmentFileId() {
        return this.attachmentFileId;
    }

    public Long getRequestedByCeoId() {
        return this.requestedByCeoId;
    }

    public LocalDateTime getProcessedAt() {
        return this.processedAt;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}
