package com.tastyhouse.infrastructure.product.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "PRODUCT_REPRESENTATIVE_REQUEST")
public class ProductRepresentativeRequestJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private ApprovalStatus status;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    protected ProductRepresentativeRequestJpaEntity() {
    }

    private ProductRepresentativeRequestJpaEntity(
        Long productId,
        Long shopId,
        ApprovalStatus status,
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
        ApprovalStatus status,
        String rejectReason
    ) {
        return new ProductRepresentativeRequestJpaEntity(productId, shopId, status, rejectReason);
    }

    void applyChanges(ApprovalStatus status, String rejectReason) {
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

    public ApprovalStatus getStatus() {
        return this.status;
    }

    public String getRejectReason() {
        return this.rejectReason;
    }
}
