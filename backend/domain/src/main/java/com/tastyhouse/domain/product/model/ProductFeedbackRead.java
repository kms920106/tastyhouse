package com.tastyhouse.domain.product.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.shop.vo.ShopId;

public class ProductFeedbackRead {
    private final Long id;
    private final ShopId shopId;
    private LocalDateTime readAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private ProductFeedbackRead(
        Long id,
        ShopId shopId,
        LocalDateTime readAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.shopId = shopId;
        this.readAt = readAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ProductFeedbackRead of(ShopId shopId, LocalDateTime readAt) {
        return new ProductFeedbackRead(null, shopId, readAt, null, null);
    }

    public static ProductFeedbackRead reconstitute(
        Long id,
        ShopId shopId,
        LocalDateTime readAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ProductFeedbackRead(id, shopId, readAt, createdAt, updatedAt);
    }

    public void markRead(LocalDateTime readAt) {
        if (this.readAt == null || readAt.isAfter(this.readAt)) {
            this.readAt = readAt;
        }
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public LocalDateTime getReadAt() {
        return this.readAt;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}
