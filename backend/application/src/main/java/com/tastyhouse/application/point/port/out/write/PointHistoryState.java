package com.tastyhouse.application.point.port.out.write;

import java.time.LocalDateTime;

public record PointHistoryState(
    Long id,
    Long memberId,
    String pointType,
    Integer pointAmount,
    String reason,
    LocalDateTime createdAt
) {
}
