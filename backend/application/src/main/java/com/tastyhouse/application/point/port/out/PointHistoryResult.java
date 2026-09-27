package com.tastyhouse.application.point.port.out;

import java.time.LocalDateTime;

public record PointHistoryResult(
    String pointType,
    Integer pointAmount,
    String reason,
    LocalDateTime createdAt
) {
}
