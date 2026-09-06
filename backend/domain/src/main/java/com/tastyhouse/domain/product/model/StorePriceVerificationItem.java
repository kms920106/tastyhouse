package com.tastyhouse.domain.product.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductPriceId;
import com.tastyhouse.domain.product.vo.StorePriceVerificationId;

public class StorePriceVerificationItem {
    private final Long id;
    private final StorePriceVerificationId verificationId;
    private final ProductId productId;
    private final ProductPriceId productPriceId;
    private final Integer storePrice;
    private final boolean applyPickupSamePrice;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private StorePriceVerificationItem(
        Long id,
        StorePriceVerificationId verificationId,
        ProductId productId,
        ProductPriceId productPriceId,
        Integer storePrice,
        boolean applyPickupSamePrice,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.verificationId = verificationId;
        this.productId = productId;
        this.productPriceId = productPriceId;
        this.storePrice = storePrice;
        this.applyPickupSamePrice = applyPickupSamePrice;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static StorePriceVerificationItem of(
        StorePriceVerificationId verificationId,
        ProductId productId,
        ProductPriceId productPriceId,
        Integer storePrice,
        boolean applyPickupSamePrice
    ) {
        return new StorePriceVerificationItem(
            null,
            verificationId,
            productId,
            productPriceId,
            storePrice,
            applyPickupSamePrice,
            null,
            null
        );
    }

    public static StorePriceVerificationItem reconstitute(
        Long id,
        StorePriceVerificationId verificationId,
        ProductId productId,
        ProductPriceId productPriceId,
        Integer storePrice,
        boolean applyPickupSamePrice,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new StorePriceVerificationItem(
            id,
            verificationId,
            productId,
            productPriceId,
            storePrice,
            applyPickupSamePrice,
            createdAt,
            updatedAt
        );
    }

    public Long getId() {
        return this.id;
    }

    public StorePriceVerificationId getVerificationId() {
        return this.verificationId;
    }

    public ProductId getProductId() {
        return this.productId;
    }

    public ProductPriceId getProductPriceId() {
        return this.productPriceId;
    }

    public Integer getStorePrice() {
        return this.storePrice;
    }

    public boolean isApplyPickupSamePrice() {
        return this.applyPickupSamePrice;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}
