package com.tastyhouse.application.product.service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import com.tastyhouse.domain.shared.model.DayType;
import com.tastyhouse.application.product.port.out.ProductExposureWindow;

public final class ProductExposureWindows {
    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");

    private ProductExposureWindows() {
    }

    public static ProductExposureWindow now() {
        return at(LocalDateTime.now(SERVICE_ZONE));
    }

    public static ProductExposureWindow at(LocalDateTime now) {
        DayOfWeek today = now.getDayOfWeek();
        return new ProductExposureWindow(
            now,
            dayTypesApplyingTo(today),
            dayTypesApplyingTo(today.minus(1))
        );
    }

    private static Set<String> dayTypesApplyingTo(DayOfWeek dayOfWeek) {
        return Arrays.stream(DayType.values())
            .filter(dayType -> dayType.appliesTo(dayOfWeek, false))
            .map(DayType::name)
            .collect(Collectors.toUnmodifiableSet());
    }
}
