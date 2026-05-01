package com.tastyhouse.core.entity.rank.dto;

import java.time.LocalDateTime;

public record RankDurationDto(
    LocalDateTime startAt,
    LocalDateTime endAt
) {
}
