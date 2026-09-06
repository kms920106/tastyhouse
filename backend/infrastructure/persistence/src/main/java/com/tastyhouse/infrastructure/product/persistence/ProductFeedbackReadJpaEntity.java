package com.tastyhouse.infrastructure.product.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "PRODUCT_FEEDBACK_READ")
public class ProductFeedbackReadJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "read_at", nullable = false)
    private LocalDateTime readAt;

    protected ProductFeedbackReadJpaEntity() {
    }

    private ProductFeedbackReadJpaEntity(Long shopId, LocalDateTime readAt) {
        this.shopId = shopId;
        this.readAt = readAt;
    }

    static ProductFeedbackReadJpaEntity create(Long shopId, LocalDateTime readAt) {
        return new ProductFeedbackReadJpaEntity(shopId, readAt);
    }

    void applyChanges(LocalDateTime readAt) {
        this.readAt = readAt;
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public LocalDateTime getReadAt() {
        return this.readAt;
    }
}
