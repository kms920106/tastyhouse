package com.tastyhouse.application.product.port.out;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.tastyhouse.domain.product.model.ProductHiddenReason;

public record ProductExposureViewResult(
    LocalDate startDate,
    LocalDate endDate,
    List<Hour> hours,
    boolean exposed,
    ProductHiddenReason hiddenReason
) {

    public record Hour(
        String dayType,
        LocalTime startTime,
        LocalTime endTime
    ) {
    }
}
