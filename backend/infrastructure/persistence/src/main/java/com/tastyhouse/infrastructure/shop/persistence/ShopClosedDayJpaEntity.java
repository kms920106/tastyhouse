package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.shop.model.ClosedDayType;

@Entity
@Table(name = "SHOP_CLOSED_DAY")
public class ShopClosedDayJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Enumerated(EnumType.STRING)
    @Column(name = "closed_day_type", nullable = false, length = 50, columnDefinition = "VARCHAR(50)")
    private ClosedDayType closedDayType;

    protected ShopClosedDayJpaEntity() {
    }

    private ShopClosedDayJpaEntity(Long shopId, ClosedDayType closedDayType) {
        this.shopId = shopId;
        this.closedDayType = closedDayType;
    }

    static ShopClosedDayJpaEntity create(Long shopId, ClosedDayType closedDayType) {
        return new ShopClosedDayJpaEntity(shopId, closedDayType);
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public ClosedDayType getClosedDayType() {
        return this.closedDayType;
    }
}
