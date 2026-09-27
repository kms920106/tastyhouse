package com.tastyhouse.application.product.port.out.write;

import java.time.LocalTime;

public record ProductExposureHourState(
    Long id,
    Long productId,
    String dayType,
    LocalTime startTime,
    LocalTime endTime
) {
}
