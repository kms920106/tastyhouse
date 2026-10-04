package com.tastyhouse.application.event.port.in;

import java.time.LocalDateTime;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record EventWinnerCreateCommand(
    Long eventId,
    Integer rankNo,
    String winnerName,
    String phoneNumber,
    LocalDateTime announcedAt
) {

    public EventWinnerCreateCommand {
        if (eventId == null || rankNo == null || winnerName == null || phoneNumber == null || announcedAt == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
