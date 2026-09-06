package com.tastyhouse.domain.shop.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ShopDeliveryAreaAdjustmentRequest {
    private final Long id;
    private final ShopId shopId;
    private final String counterpartShopName;
    private final String counterpartBusinessNumber;
    private final String franchiseName;
    private final String reason;
    private final UploadedFileId consentFileId;
    private DeliveryAreaAdjustmentStatus status;
    private String rejectReason;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private ShopDeliveryAreaAdjustmentRequest(
        Long id,
        ShopId shopId,
        String counterpartShopName,
        String counterpartBusinessNumber,
        String franchiseName,
        String reason,
        UploadedFileId consentFileId,
        DeliveryAreaAdjustmentStatus status,
        String rejectReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.shopId = shopId;
        this.counterpartShopName = counterpartShopName;
        this.counterpartBusinessNumber = counterpartBusinessNumber;
        this.franchiseName = franchiseName;
        this.reason = reason;
        this.consentFileId = consentFileId;
        this.status = status;
        this.rejectReason = rejectReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ShopDeliveryAreaAdjustmentRequest of(
        ShopId shopId,
        String counterpartShopName,
        String counterpartBusinessNumber,
        String franchiseName,
        String reason,
        UploadedFileId consentFileId
    ) {
        return new ShopDeliveryAreaAdjustmentRequest(
            null,
            shopId,
            counterpartShopName,
            counterpartBusinessNumber,
            franchiseName,
            reason,
            consentFileId,
            DeliveryAreaAdjustmentStatus.PENDING,
            null,
            null,
            null
        );
    }

    public static ShopDeliveryAreaAdjustmentRequest reconstitute(
        Long id,
        ShopId shopId,
        String counterpartShopName,
        String counterpartBusinessNumber,
        String franchiseName,
        String reason,
        UploadedFileId consentFileId,
        DeliveryAreaAdjustmentStatus status,
        String rejectReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ShopDeliveryAreaAdjustmentRequest(
            id,
            shopId,
            counterpartShopName,
            counterpartBusinessNumber,
            franchiseName,
            reason,
            consentFileId,
            status,
            rejectReason,
            createdAt,
            updatedAt
        );
    }

    public void startProgress() {
        if (this.status != DeliveryAreaAdjustmentStatus.PENDING) {
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_AREA_ADJUSTMENT_REQUEST_NOT_PENDING);
        }
        this.status = DeliveryAreaAdjustmentStatus.IN_PROGRESS;
    }

    public void complete() {
        if (this.status != DeliveryAreaAdjustmentStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_AREA_ADJUSTMENT_REQUEST_NOT_IN_PROGRESS);
        }
        this.status = DeliveryAreaAdjustmentStatus.COMPLETED;
    }

    public void reject(String reason) {
        if (this.status == DeliveryAreaAdjustmentStatus.COMPLETED
            || this.status == DeliveryAreaAdjustmentStatus.REJECTED
            || this.status == DeliveryAreaAdjustmentStatus.CANCELED) {
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_AREA_ADJUSTMENT_REQUEST_ALREADY_CLOSED);
        }
        this.status = DeliveryAreaAdjustmentStatus.REJECTED;
        this.rejectReason = reason;
    }

    public void cancel() {
        if (this.status != DeliveryAreaAdjustmentStatus.PENDING) {
            throw new BusinessException(ErrorCode.SHOP_REQUEST_NOT_CANCELABLE);
        }
        this.status = DeliveryAreaAdjustmentStatus.CANCELED;
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public String getCounterpartShopName() {
        return this.counterpartShopName;
    }

    public String getCounterpartBusinessNumber() {
        return this.counterpartBusinessNumber;
    }

    public String getFranchiseName() {
        return this.franchiseName;
    }

    public String getReason() {
        return this.reason;
    }

    public UploadedFileId getConsentFileId() {
        return this.consentFileId;
    }

    public DeliveryAreaAdjustmentStatus getStatus() {
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
