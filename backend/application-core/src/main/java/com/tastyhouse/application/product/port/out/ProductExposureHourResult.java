package com.tastyhouse.application.product.port.out;

import java.time.LocalTime;

public record ProductExposureHourResult(
    String dayType,
    LocalTime startTime,
    LocalTime endTime
) {
}
