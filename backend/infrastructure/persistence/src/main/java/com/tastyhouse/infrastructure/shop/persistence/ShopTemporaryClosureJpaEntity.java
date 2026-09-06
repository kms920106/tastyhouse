package com.tastyhouse.infrastructure.shop.persistence;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "SHOP_TEMPORARY_CLOSURE")
public class ShopTemporaryClosureJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    protected ShopTemporaryClosureJpaEntity() {
    }

    private ShopTemporaryClosureJpaEntity(Long shopId, LocalDate startDate, LocalDate endDate) {
        this.shopId = shopId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    static ShopTemporaryClosureJpaEntity create(Long shopId, LocalDate startDate, LocalDate endDate) {
        return new ShopTemporaryClosureJpaEntity(shopId, startDate, endDate);
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public LocalDate getStartDate() {
        return this.startDate;
    }

    public LocalDate getEndDate() {
        return this.endDate;
    }
}
