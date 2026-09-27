package com.tastyhouse.application.event.port.out.write;

import java.time.LocalDateTime;

public record EventWinnerState(
    Long id,
    Long eventId,
    Integer rankNo,
    String winnerName,
    String phoneNumber,
    LocalDateTime announcedAt,
    boolean deleted
) {
}
