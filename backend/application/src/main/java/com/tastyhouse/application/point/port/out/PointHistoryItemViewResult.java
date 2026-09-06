package com.tastyhouse.application.point.port.out;

import java.time.LocalDate;

public record PointHistoryItemViewResult(
    String reason,
    LocalDate date,
    Integer pointAmount,
    String pointType
) {
}
