package com.tastyhouse.application.product.port.out;

import java.time.LocalDateTime;
import java.util.Set;

public record ProductExposureWindow(
    LocalDateTime now,
    Set<String> todayDayTypes,
    Set<String> previousDayDayTypes
) {

    public ProductExposureWindow {
        todayDayTypes = Set.copyOf(todayDayTypes);
        previousDayDayTypes = Set.copyOf(previousDayDayTypes);
    }
}
