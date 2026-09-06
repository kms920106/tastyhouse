package com.tastyhouse.application.product.port.out;

import java.time.LocalDate;

public record ProductExposurePeriodResult(
    Long productId,
    Long shopId,
    LocalDate startDate,
    LocalDate endDate
) {
}
