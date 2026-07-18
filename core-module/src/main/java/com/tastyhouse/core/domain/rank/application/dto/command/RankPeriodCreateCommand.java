package com.tastyhouse.core.domain.rank.application.dto.command;

import java.time.LocalDateTime;

public record RankPeriodCreateCommand(
    LocalDateTime startAt,
    LocalDateTime endAt,
    boolean visible
) {

    public static RankPeriodCreateCommand of(
        LocalDateTime startAt,
        LocalDateTime endAt,
        boolean visible
    ) {
        return new RankPeriodCreateCommand(startAt, endAt, visible);
    }
}
