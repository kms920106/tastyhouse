package com.tastyhouse.application.event.port.out;

import java.time.LocalDateTime;

public record EventWinnerResult(
    Long id,
    Long eventId,
    Integer rankNo,
    String winnerName,
    String phoneNumber,
    LocalDateTime announcedAt
) {
}
