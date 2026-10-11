package com.tastyhouse.application.product.port.out;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record ProductExposureViewResult(
    LocalDate startDate,
    LocalDate endDate,
    List<Hour> hours,
    boolean exposed,
    String hiddenReason
) {

    public record Hour(
        String dayType,
        LocalTime startTime,
        LocalTime endTime
    ) {
    }
}
