package com.tastyhouse.core.domain.rank.application.dto.result;

import java.time.LocalDateTime;

public record RankDurationResult(
    LocalDateTime startAt,
    LocalDateTime endAt
) {
}
