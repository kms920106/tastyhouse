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
@Table(name = "SHOP_BREAK_TIME")
public class ShopBreakTimeJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private DayType dayType;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    protected ShopBreakTimeJpaEntity() {
    }

    private ShopBreakTimeJpaEntity(Long shopId, DayType dayType, LocalTime startTime, LocalTime endTime) {
        this.shopId = shopId;
        this.dayType = dayType;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    static ShopBreakTimeJpaEntity create(Long shopId, DayType dayType, LocalTime startTime, LocalTime endTime) {
        return new ShopBreakTimeJpaEntity(shopId, dayType, startTime, endTime);
    }

    void applyChanges(DayType dayType, LocalTime startTime, LocalTime endTime) {
        this.dayType = dayType;
        this.startTime = startTime;
        this.endTime = endTime;
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

    public LocalTime getStartTime() {
        return this.startTime;
    }

    public LocalTime getEndTime() {
        return this.endTime;
    }
}
