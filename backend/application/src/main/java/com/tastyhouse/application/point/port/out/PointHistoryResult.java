package com.tastyhouse.application.point.port.out;

import java.time.LocalDateTime;

import com.tastyhouse.domain.point.model.PointType;

public record PointHistoryResult(
    PointType pointType,
    Integer pointAmount,
    String reason,
    LocalDateTime createdAt
) {
}
