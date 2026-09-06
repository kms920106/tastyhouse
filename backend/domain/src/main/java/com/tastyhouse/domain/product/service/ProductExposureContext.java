package com.tastyhouse.domain.product.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.domain.product.model.ProductExposureHour;

public record ProductExposureContext(
    boolean visible,
    LocalDate exposureStartDate,
    LocalDate exposureEndDate,
    List<ProductExposureHour> hours,
    LocalDateTime now,
    boolean publicHoliday,
    boolean previousDayPublicHoliday
) {
    public ProductExposureContext {
        hours = hours == null ? List.of() : List.copyOf(hours);
    }

    public static ProductExposureContext of(
        boolean visible,
        LocalDate exposureStartDate,
        LocalDate exposureEndDate,
        List<ProductExposureHour> hours,
        LocalDateTime now,
        boolean publicHoliday,
        boolean previousDayPublicHoliday
    ) {
        return new ProductExposureContext(
            visible,
            exposureStartDate,
            exposureEndDate,
            hours,
            now,
            publicHoliday,
            previousDayPublicHoliday
        );
    }
}
