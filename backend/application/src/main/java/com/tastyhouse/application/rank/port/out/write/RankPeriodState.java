package com.tastyhouse.application.rank.port.out.write;

import java.time.LocalDateTime;

public record RankPeriodState(
    Long id,
    LocalDateTime startAt,
    LocalDateTime endAt,
    boolean visible,
    boolean deleted,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
