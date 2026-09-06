package com.tastyhouse.application.rank.port.in;

import java.time.LocalDate;

public record RankAggregateCommand(
    String type,
    LocalDate baseDate,
    Integer limit
) {
}
