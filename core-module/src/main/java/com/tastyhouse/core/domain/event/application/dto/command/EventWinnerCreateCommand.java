package com.tastyhouse.core.domain.event.application.dto.command;

import java.time.LocalDateTime;

public record EventWinnerCreateCommand(
    Integer rankNo,
    String winnerName,
    String phoneNumber,
    LocalDateTime announcedAt
) {

    public static EventWinnerCreateCommand of(
        Integer rankNo,
        String winnerName,
        String phoneNumber,
        LocalDateTime announcedAt
    ) {
        return new EventWinnerCreateCommand(rankNo, winnerName, phoneNumber, announcedAt);
    }
}
