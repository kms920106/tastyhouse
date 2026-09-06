package com.tastyhouse.application.rank.port.out;

import java.time.LocalDateTime;

public record RankPeriodResult(
    Long id,
    LocalDateTime startAt,
    LocalDateTime endAt,
    boolean visible,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
