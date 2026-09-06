package com.tastyhouse.domain.product.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductRepresentativeRequestId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ProductRepresentativeRequest {
    private final Long id;
    private final ProductId productId;
    private final ShopId shopId;
    private ApprovalStatus status;
    private String rejectReason;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private ProductRepresentativeRequest(
        Long id,
        ProductId productId,
        ShopId shopId,
        ApprovalStatus status,
        String rejectReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.productId = productId;
        this.shopId = shopId;
        this.status = status;
        this.rejectReason = rejectReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ProductRepresentativeRequest of(ProductId productId, ShopId shopId) {
        return new ProductRepresentativeRequest(
            null,
            productId,
            shopId,
            ApprovalStatus.PENDING,
            null,
            null,
            null
        );
    }

    public static ProductRepresentativeRequest reconstitute(
        Long id,
        ProductId productId,
        ShopId shopId,
        ApprovalStatus status,
        String rejectReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ProductRepresentativeRequest(
            id,
            productId,
            shopId,
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

    public ShopId getShopId() {
        return this.shopId;
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

    public ProductRepresentativeRequestId getRequestId() {
        return ProductRepresentativeRequestId.of(this.id);
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
            throw new BusinessException(ErrorCode.PRODUCT_REPRESENTATIVE_REQUEST_NOT_PENDING);
        }
    }
}
