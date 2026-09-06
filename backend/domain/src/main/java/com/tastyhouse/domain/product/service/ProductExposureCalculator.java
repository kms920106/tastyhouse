package com.tastyhouse.domain.product.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.tastyhouse.domain.product.model.ProductExposureHour;
import com.tastyhouse.domain.product.model.ProductHiddenReason;

public class ProductExposureCalculator {
    public ProductExposureResult calculate(ProductExposureContext context) {
        if (!context.visible()) {
            return ProductExposureResult.ofHidden(ProductHiddenReason.MANUALLY_HIDDEN);
        }

        LocalDate today = context.now().toLocalDate();
        if (context.exposureStartDate() != null && today.isBefore(context.exposureStartDate())) {
            return ProductExposureResult.ofHidden(ProductHiddenReason.BEFORE_EXPOSURE_PERIOD);
        }
        if (context.exposureEndDate() != null && today.isAfter(context.exposureEndDate())) {
            return ProductExposureResult.ofHidden(ProductHiddenReason.AFTER_EXPOSURE_PERIOD);
        }

        if (context.hours().isEmpty()) {
            return ProductExposureResult.ofExposed();
        }
        if (matchesHours(context)) {
            return ProductExposureResult.ofExposed();
        }
        return ProductExposureResult.ofHidden(ProductHiddenReason.OUT_OF_EXPOSURE_HOURS);
    }

    private boolean matchesHours(ProductExposureContext context) {
        DayOfWeek today = context.now().getDayOfWeek();
        LocalTime time = context.now().toLocalTime();

        for (ProductExposureHour hour : context.hours()) {
            if (hour.getDayType().appliesTo(today, context.publicHoliday())
                && coversToday(hour, time)) {
                return true;
            }
        }

        DayOfWeek yesterday = today.minus(1);
        for (ProductExposureHour hour : context.hours()) {
            if (hour.getDayType().appliesTo(yesterday, context.previousDayPublicHoliday())
                && coversAsOvernightTail(hour, time)) {
                return true;
            }
        }
        return false;
    }

    private boolean coversToday(ProductExposureHour hour, LocalTime time) {
        if (hour.isAllDay()) {
            return true;
        }
        if (hour.isOvernight()) {
            return !time.isBefore(hour.getStartTime());
        }
        return !time.isBefore(hour.getStartTime()) && time.isBefore(hour.getEndTime());
    }

    private boolean coversAsOvernightTail(ProductExposureHour hour, LocalTime time) {
        return hour.isOvernight() && time.isBefore(hour.getEndTime());
    }

    public ProductExposureResult calculate(ProductExposureContext base, List<ProductExposureHour> hours) {
        return calculate(ProductExposureContext.of(
            base.visible(),
            base.exposureStartDate(),
            base.exposureEndDate(),
            hours,
            base.now(),
            base.publicHoliday(),
            base.previousDayPublicHoliday()
        ));
    }
}
