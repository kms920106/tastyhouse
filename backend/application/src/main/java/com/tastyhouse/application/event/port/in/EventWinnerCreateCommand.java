package com.tastyhouse.application.event.port.in;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record EventWinnerCreateCommand(
    Long eventId,
    Integer rankNo,
    String winnerName,
    String phoneNumber,
    LocalDateTime announcedAt
) {
    public EventWinnerCreateCommand {
        if (eventId == null || rankNo == null || winnerName == null || phoneNumber == null || announcedAt == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
