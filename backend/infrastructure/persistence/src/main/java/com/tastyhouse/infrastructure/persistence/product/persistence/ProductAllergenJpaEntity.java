package com.tastyhouse.infrastructure.persistence.product.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.persistence.shared.persistence.BaseEntity;

@Entity
@Table(name = "PRODUCT_ALLERGEN")
class ProductAllergenJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "allergen_type", nullable = false, length = 30, columnDefinition = "VARCHAR(30)")
    private String allergenType;

    protected ProductAllergenJpaEntity() {
    }

    private ProductAllergenJpaEntity(Long productId, String allergenType) {
        this.productId = productId;
        this.allergenType = allergenType;
    }

    static ProductAllergenJpaEntity create(Long productId, String allergenType) {
        return new ProductAllergenJpaEntity(productId, allergenType);
    }

    public Long getId() {
        return this.id;
    }

    public Long getProductId() {
        return this.productId;
    }

    public String getAllergenType() {
        return this.allergenType;
    }
}
