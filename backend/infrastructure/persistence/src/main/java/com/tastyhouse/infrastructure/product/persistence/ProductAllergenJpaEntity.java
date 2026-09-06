package com.tastyhouse.infrastructure.product.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.product.model.AllergenType;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "PRODUCT_ALLERGEN")
public class ProductAllergenJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Enumerated(EnumType.STRING)
    @Column(name = "allergen_type", nullable = false, length = 30, columnDefinition = "VARCHAR(30)")
    private AllergenType allergenType;

    protected ProductAllergenJpaEntity() {
    }

    private ProductAllergenJpaEntity(Long productId, AllergenType allergenType) {
        this.productId = productId;
        this.allergenType = allergenType;
    }

    static ProductAllergenJpaEntity create(Long productId, AllergenType allergenType) {
        return new ProductAllergenJpaEntity(productId, allergenType);
    }

    public Long getId() {
        return this.id;
    }

    public Long getProductId() {
        return this.productId;
    }

    public AllergenType getAllergenType() {
        return this.allergenType;
    }
}
