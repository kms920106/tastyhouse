package com.tastyhouse.infrastructure.shop.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.model.SuspensionReason;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "SHOP_SUSPENSION")
public class ShopSuspensionJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 30, columnDefinition = "VARCHAR(30)")
    private SuspensionReason reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_method", length = 20, columnDefinition = "VARCHAR(20)")
    private OrderMethod orderMethod;

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
        SuspensionReason reason,
        OrderMethod orderMethod,
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
        SuspensionReason reason,
        OrderMethod orderMethod,
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

    public SuspensionReason getReason() {
        return this.reason;
    }

    public OrderMethod getOrderMethod() {
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
