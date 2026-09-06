package com.tastyhouse.domain.product.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.product.vo.ProductId;

public class ProductAllergen {
    private final Long id;
    private final ProductId productId;
    private final AllergenType allergenType;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private ProductAllergen(
        Long id,
        ProductId productId,
        AllergenType allergenType,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.productId = productId;
        this.allergenType = allergenType;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ProductAllergen of(ProductId productId, AllergenType allergenType) {
        return new ProductAllergen(null, productId, allergenType, null, null);
    }

    public static ProductAllergen reconstitute(
        Long id,
        ProductId productId,
        AllergenType allergenType,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ProductAllergen(id, productId, allergenType, createdAt, updatedAt);
    }

    public Long getId() {
        return this.id;
    }

    public ProductId getProductId() {
        return this.productId;
    }

    public AllergenType getAllergenType() {
        return this.allergenType;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}
