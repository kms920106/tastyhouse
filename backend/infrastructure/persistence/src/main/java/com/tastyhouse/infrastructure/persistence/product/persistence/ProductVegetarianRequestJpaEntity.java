package com.tastyhouse.infrastructure.persistence.product.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.persistence.shared.persistence.BaseEntity;

@Entity
@Table(name = "PRODUCT_VEGETARIAN_REQUEST")
class ProductVegetarianRequestJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "vegetarian_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private String vegetarianType;

    @Column(name = "ingredients", nullable = false, length = 1000)
    private String ingredients;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private String status;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    protected ProductVegetarianRequestJpaEntity() {
    }

    private ProductVegetarianRequestJpaEntity(
        Long productId,
        String vegetarianType,
        String ingredients,
        String description,
        String status,
        String rejectReason
    ) {
        this.productId = productId;
        this.vegetarianType = vegetarianType;
        this.ingredients = ingredients;
        this.description = description;
        this.status = status;
        this.rejectReason = rejectReason;
    }

    static ProductVegetarianRequestJpaEntity create(
        Long productId,
        String vegetarianType,
        String ingredients,
        String description,
        String status,
        String rejectReason
    ) {
        return new ProductVegetarianRequestJpaEntity(
            productId, vegetarianType, ingredients, description, status, rejectReason
        );
    }

    void applyChanges(String status, String rejectReason) {
        this.status = status;
        this.rejectReason = rejectReason;
    }

    public Long getId() {
        return this.id;
    }

    public Long getProductId() {
        return this.productId;
    }

    public String getVegetarianType() {
        return this.vegetarianType;
    }

    public String getIngredients() {
        return this.ingredients;
    }

    public String getDescription() {
        return this.description;
    }

    public String getStatus() {
        return this.status;
    }

    public String getRejectReason() {
        return this.rejectReason;
    }
}
