package com.tastyhouse.infrastructure.product.persistence;

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
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "PRODUCT_EXPOSURE_HOUR")
public class ProductExposureHourJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private DayType dayType;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    protected ProductExposureHourJpaEntity() {
    }

    private ProductExposureHourJpaEntity(
        Long productId,
        DayType dayType,
        LocalTime startTime,
        LocalTime endTime
    ) {
        this.productId = productId;
        this.dayType = dayType;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    static ProductExposureHourJpaEntity create(
        Long productId,
        DayType dayType,
        LocalTime startTime,
        LocalTime endTime
    ) {
        return new ProductExposureHourJpaEntity(productId, dayType, startTime, endTime);
    }

    public Long getId() {
        return this.id;
    }

    public Long getProductId() {
        return this.productId;
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
