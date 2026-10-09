package com.tastyhouse.infrastructure.jpa.product.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.jpa.shared.persistence.BaseEntity;

@Entity
@Table(name = "PRODUCT_REPRESENTATIVE_REQUEST")
class ProductRepresentativeRequestJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private String status;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    protected ProductRepresentativeRequestJpaEntity() {
    }

    private ProductRepresentativeRequestJpaEntity(
        Long productId,
        Long shopId,
        String status,
        String rejectReason
    ) {
        this.productId = productId;
        this.shopId = shopId;
        this.status = status;
        this.rejectReason = rejectReason;
    }

    static ProductRepresentativeRequestJpaEntity create(
        Long productId,
        Long shopId,
        String status,
        String rejectReason
    ) {
        return new ProductRepresentativeRequestJpaEntity(productId, shopId, status, rejectReason);
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

    public Long getShopId() {
        return this.shopId;
    }

    public String getStatus() {
        return this.status;
    }

    public String getRejectReason() {
        return this.rejectReason;
    }
}
