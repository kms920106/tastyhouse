package com.tastyhouse.domain.product.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.vo.StorePriceVerificationId;
import com.tastyhouse.domain.shop.vo.ShopId;

public class StorePriceVerification {
    private final Long id;
    private final ShopId shopId;
    private final UploadedFileId priceListFileId;
    private StorePriceVerificationStatus status;
    private String rejectReason;
    private final Long requestedByCeoId;
    private LocalDateTime processedAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private StorePriceVerification(
        Long id,
        ShopId shopId,
        UploadedFileId priceListFileId,
        StorePriceVerificationStatus status,
        String rejectReason,
        Long requestedByCeoId,
        LocalDateTime processedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.shopId = shopId;
        this.priceListFileId = priceListFileId;
        this.status = status;
        this.rejectReason = rejectReason;
        this.requestedByCeoId = requestedByCeoId;
        this.processedAt = processedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static StorePriceVerification of(
        ShopId shopId,
        UploadedFileId priceListFileId,
        Long requestedByCeoId
    ) {
        return new StorePriceVerification(
            null,
            shopId,
            priceListFileId,
            StorePriceVerificationStatus.PENDING,
            null,
            requestedByCeoId,
            null,
            null,
            null
        );
    }

    public static StorePriceVerification reconstitute(
        Long id,
        ShopId shopId,
        UploadedFileId priceListFileId,
        StorePriceVerificationStatus status,
        String rejectReason,
        Long requestedByCeoId,
        LocalDateTime processedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new StorePriceVerification(
            id,
            shopId,
            priceListFileId,
            status,
            rejectReason,
            requestedByCeoId,
            processedAt,
            createdAt,
            updatedAt
        );
    }

    public void startReview(LocalDateTime now) {
        if (this.status != StorePriceVerificationStatus.PENDING) {
            throw new BusinessException(ErrorCode.SHOP_STORE_PRICE_VERIFICATION_NOT_PENDING);
        }
        this.status = StorePriceVerificationStatus.IN_PROGRESS;
        this.processedAt = now;
    }

    public void approve(LocalDateTime now) {
        requireOpen();
        this.status = StorePriceVerificationStatus.APPROVED;
        this.rejectReason = null;
        this.processedAt = now;
    }

    public void reject(String rejectReason, LocalDateTime now) {
        requireOpen();
        this.status = StorePriceVerificationStatus.REJECTED;
        this.rejectReason = rejectReason;
        this.processedAt = now;
    }

    public void cancel(LocalDateTime now) {
        requireOpen();
        this.status = StorePriceVerificationStatus.CANCELED;
        this.rejectReason = null;
        this.processedAt = now;
    }

    private void requireOpen() {
        if (!this.status.isOpen()) {
            throw new BusinessException(ErrorCode.SHOP_STORE_PRICE_VERIFICATION_NOT_PENDING);
        }
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public UploadedFileId getPriceListFileId() {
        return this.priceListFileId;
    }

    public StorePriceVerificationStatus getStatus() {
        return this.status;
    }

    public String getRejectReason() {
        return this.rejectReason;
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

    public StorePriceVerificationId getVerificationId() {
        return StorePriceVerificationId.of(this.id);
    }
}
