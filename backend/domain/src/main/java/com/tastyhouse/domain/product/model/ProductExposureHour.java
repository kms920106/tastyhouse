package com.tastyhouse.domain.product.model;

import java.time.LocalTime;

import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shared.model.DayType;

public class ProductExposureHour {
    private final Long id;
    private final ProductId productId;
    private final DayType dayType;
    private final LocalTime startTime;
    private final LocalTime endTime;

    private ProductExposureHour(
        Long id,
        ProductId productId,
        DayType dayType,
        LocalTime startTime,
        LocalTime endTime
    ) {
        this.id = id;
        this.productId = productId;
        this.dayType = dayType;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public static ProductExposureHour of(
        ProductId productId,
        DayType dayType,
        LocalTime startTime,
        LocalTime endTime
    ) {
        return new ProductExposureHour(null, productId, dayType, startTime, endTime);
    }

    public static ProductExposureHour reconstitute(
        Long id,
        ProductId productId,
        DayType dayType,
        LocalTime startTime,
        LocalTime endTime
    ) {
        return new ProductExposureHour(id, productId, dayType, startTime, endTime);
    }

    public Long getId() {
        return this.id;
    }

    public ProductId getProductId() {
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

    public boolean isAllDay() {
        return this.startTime == null || this.endTime == null;
    }

    public boolean isOvernight() {
        return !isAllDay() && this.endTime.isBefore(this.startTime);
    }
}
