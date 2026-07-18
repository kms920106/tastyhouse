package com.tastyhouse.core.domain.rank.application.dto.command;

import java.time.LocalDateTime;

public record RankPeriodUpdateCommand(
    LocalDateTime startAt,
    LocalDateTime endAt,
    boolean visible
) {

    public static RankPeriodUpdateCommand of(
        LocalDateTime startAt,
        LocalDateTime endAt,
        boolean visible
    ) {
        return new RankPeriodUpdateCommand(startAt, endAt, visible);
    }
}
