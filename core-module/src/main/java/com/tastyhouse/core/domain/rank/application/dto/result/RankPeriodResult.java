package com.tastyhouse.core.domain.rank.application.dto.result;

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
