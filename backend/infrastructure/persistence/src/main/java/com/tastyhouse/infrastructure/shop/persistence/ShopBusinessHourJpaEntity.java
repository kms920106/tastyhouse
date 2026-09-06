package com.tastyhouse.infrastructure.shop.persistence;

import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.shared.model.DayType;

@Entity
@Table(name = "SHOP_BUSINESS_HOUR")
public class ShopBusinessHourJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private DayType dayType;

    @Column(name = "open_time")
    private LocalTime openTime;

    @Column(name = "close_time")
    private LocalTime closeTime;

    @Column(name = "is_closed")
    private Boolean isClosed;

    @Column(name = "is_open_24_hours")
    private Boolean is24Hours;

    protected ShopBusinessHourJpaEntity() {
    }

    private ShopBusinessHourJpaEntity(
        Long shopId,
        DayType dayType,
        LocalTime openTime,
        LocalTime closeTime,
        Boolean isClosed,
        Boolean is24Hours
    ) {
        this.shopId = shopId;
        this.dayType = dayType;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.isClosed = isClosed;
        this.is24Hours = is24Hours;
    }

    static ShopBusinessHourJpaEntity create(
        Long shopId,
        DayType dayType,
        LocalTime openTime,
        LocalTime closeTime,
        Boolean isClosed,
        Boolean is24Hours
    ) {
        return new ShopBusinessHourJpaEntity(shopId, dayType, openTime, closeTime, isClosed, is24Hours);
    }

    void applyChanges(DayType dayType, LocalTime openTime, LocalTime closeTime, Boolean isClosed, Boolean is24Hours) {
        this.dayType = dayType;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.isClosed = isClosed;
        this.is24Hours = is24Hours;
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public DayType getDayType() {
        return this.dayType;
    }

    public LocalTime getOpenTime() {
        return this.openTime;
    }

    public LocalTime getCloseTime() {
        return this.closeTime;
    }

    public Boolean getIsClosed() {
        return this.isClosed;
    }

    public Boolean getIs24Hours() {
        return this.is24Hours;
    }
}
