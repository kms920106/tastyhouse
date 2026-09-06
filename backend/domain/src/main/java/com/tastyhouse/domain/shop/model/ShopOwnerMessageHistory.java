package com.tastyhouse.domain.shop.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.shop.vo.ShopId;

public class ShopOwnerMessageHistory {
    private final Long id;
    private final ShopId shopId;
    private final String message;
    private final LocalDateTime createdAt;

    private ShopOwnerMessageHistory(Long id, ShopId shopId, String message, LocalDateTime createdAt) {
        this.id = id;
        this.shopId = shopId;
        this.message = message;
        this.createdAt = createdAt;
    }

    public static ShopOwnerMessageHistory of(ShopId shopId, String message) {
        return new ShopOwnerMessageHistory(null, shopId, message, null);
    }

    public static ShopOwnerMessageHistory reconstitute(Long id, ShopId shopId, String message, LocalDateTime createdAt) {
        return new ShopOwnerMessageHistory(id, shopId, message, createdAt);
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public String getMessage() {
        return this.message;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }
}
