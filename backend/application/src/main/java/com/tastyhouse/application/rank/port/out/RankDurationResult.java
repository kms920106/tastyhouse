package com.tastyhouse.application.rank.port.out;

import java.time.LocalDateTime;

public record RankDurationResult(
    LocalDateTime startAt,
    LocalDateTime endAt
) {
}
