package com.tastyhouse.domain.product.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductVegetarianRequestId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;

public class ProductVegetarianRequest {
    private final Long id;
    private final ProductId productId;
    private final VegetarianType vegetarianType;
    private final String ingredients;
    private final String description;
    private ApprovalStatus status;
    private String rejectReason;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private ProductVegetarianRequest(
        Long id,
        ProductId productId,
        VegetarianType vegetarianType,
        String ingredients,
        String description,
        ApprovalStatus status,
        String rejectReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.productId = productId;
        this.vegetarianType = vegetarianType;
        this.ingredients = ingredients;
        this.description = description;
        this.status = status;
        this.rejectReason = rejectReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ProductVegetarianRequest of(
        ProductId productId,
        VegetarianType vegetarianType,
        String ingredients,
        String description
    ) {
        return new ProductVegetarianRequest(
            null,
            productId,
            vegetarianType,
            ingredients,
            description,
            ApprovalStatus.PENDING,
            null,
            null,
            null
        );
    }

    public static ProductVegetarianRequest reconstitute(
        Long id,
        ProductId productId,
        VegetarianType vegetarianType,
        String ingredients,
        String description,
        ApprovalStatus status,
        String rejectReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ProductVegetarianRequest(
            id,
            productId,
            vegetarianType,
            ingredients,
            description,
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

    public VegetarianType getVegetarianType() {
        return this.vegetarianType;
    }

    public String getIngredients() {
        return this.ingredients;
    }

    public String getDescription() {
        return this.description;
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

    public ProductVegetarianRequestId getRequestId() {
        return ProductVegetarianRequestId.of(this.id);
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
            throw new BusinessException(ErrorCode.PRODUCT_VEGETARIAN_REQUEST_NOT_PENDING);
        }
    }
}
