package com.tastyhouse.infrastructure.jpa.shop.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.jpa.shared.persistence.BaseEntity;

@Entity
@Table(name = "SHOP_SUSPENSION")
class ShopSuspensionJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "reason", nullable = false, length = 30, columnDefinition = "VARCHAR(30)")
    private String reason;

    @Column(name = "order_method", length = 20, columnDefinition = "VARCHAR(20)")
    private String orderMethod;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "released_at")
    private LocalDateTime releasedAt;

    protected ShopSuspensionJpaEntity() {
    }

    private ShopSuspensionJpaEntity(
        Long shopId,
        String reason,
        String orderMethod,
        LocalDateTime startAt,
        LocalDateTime endAt,
        LocalDateTime releasedAt
    ) {
        this.shopId = shopId;
        this.reason = reason;
        this.orderMethod = orderMethod;
        this.startAt = startAt;
        this.endAt = endAt;
        this.releasedAt = releasedAt;
    }

    static ShopSuspensionJpaEntity create(
        Long shopId,
        String reason,
        String orderMethod,
        LocalDateTime startAt,
        LocalDateTime endAt,
        LocalDateTime releasedAt
    ) {
        return new ShopSuspensionJpaEntity(shopId, reason, orderMethod, startAt, endAt, releasedAt);
    }

    void applyChanges(LocalDateTime releasedAt) {
        this.releasedAt = releasedAt;
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public String getReason() {
        return this.reason;
    }

    public String getOrderMethod() {
        return this.orderMethod;
    }

    public LocalDateTime getStartAt() {
        return this.startAt;
    }

    public LocalDateTime getEndAt() {
        return this.endAt;
    }

    public LocalDateTime getReleasedAt() {
        return this.releasedAt;
    }
}
